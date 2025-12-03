package com.example.businesscardapp.service

import android.app.*
import android.content.*
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.example.businesscardapp.R
import com.example.businesscardapp.data.model.CardCallInfoResponse
import com.google.gson.Gson

class IncomingOverlayService : Service() {

    companion object {
        private const val TAG = "IncomingOverlay"
        const val ACTION_UPDATE = "CARD_INFO_UPDATE"
        private const val NOTI_CH = "overlay_foreground"
        private const val NOTI_ID = 2222
    }

    private lateinit var wm: WindowManager
    private var root: View? = null
    private var txtName: TextView? = null
    private var txtCompanyPos: TextView? = null
    private var txtMemo: TextView? = null
    private var imgProfile: ImageView? = null
    private var btnClose: ImageButton? = null

    // --- 통화 종료 시 자동 종료용 ---
    private var tm: TelephonyManager? = null
    private var phoneStateListener: PhoneStateListener? = null
    private var phoneStateCallback31: TelephonyCallback? = null

    private val phoneEndReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 일부 기기/버전에선 이 브로드캐스트가 더 잘 동작
            val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
            if (TelephonyManager.EXTRA_STATE_IDLE == state) {
                root?.visibility = View.GONE
                stopSelf()
            }
        }
    }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val json = intent?.getStringExtra("cardInfoJson")
            val phone = intent?.getStringExtra("phone")
            val card = json?.let { runCatching { Gson().fromJson(it, CardCallInfoResponse::class.java) }.getOrNull() }
            bindData(card, phone)
        }
    }

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlayView()
        startAsForeground()

        // 카드 데이터 갱신 수신
        val filter = IntentFilter(ACTION_UPDATE)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(updateReceiver, filter)
        }

        // 통화 종료 감지(브로드캐스트)
        val phoneFilter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(phoneEndReceiver, phoneFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(phoneEndReceiver, phoneFilter)
        }

        // 통화 종료 감지(콜백) - 가능한 경우만 등록
        tm = getSystemService(TELEPHONY_SERVICE) as? TelephonyManager
        if (Build.VERSION.SDK_INT >= 31) {
            val cb = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    if (state == TelephonyManager.CALL_STATE_IDLE) {
                        root?.visibility = View.GONE
                        stopSelf()
                    }
                }
            }
            runCatching { tm?.registerTelephonyCallback(mainExecutor, cb) }
            phoneStateCallback31 = cb
        } else {
            @Suppress("DEPRECATION")
            val listener = object : PhoneStateListener() {
                @Suppress("DEPRECATION")
                override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                    if (state == TelephonyManager.CALL_STATE_IDLE) {
                        root?.visibility = View.GONE
                        stopSelf()
                    }
                }
            }
            @Suppress("DEPRECATION")
            runCatching { tm?.listen(listener, PhoneStateListener.LISTEN_CALL_STATE) }
            phoneStateListener = listener
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phone = intent?.getStringExtra("phone")
        val json = intent?.getStringExtra("cardInfoJson")
        val card = json?.let { runCatching { Gson().fromJson(it, CardCallInfoResponse::class.java) }.getOrNull() }
        bindData(card, phone)
        root?.visibility = View.VISIBLE
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(updateReceiver) }
        runCatching { unregisterReceiver(phoneEndReceiver) }
        if (Build.VERSION.SDK_INT >= 31) {
            runCatching { tm?.unregisterTelephonyCallback(phoneStateCallback31 as TelephonyCallback) }
        } else {
            @Suppress("DEPRECATION")
            runCatching { tm?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE) }
        }
        root?.let { runCatching { wm.removeViewImmediate(it) } }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createOverlayView() {
        if (root != null) return
        val view = LayoutInflater.from(this).inflate(R.layout.overlay_incoming_card, null)
        txtName = view.findViewById(R.id.txtName)
        txtCompanyPos = view.findViewById(R.id.txtCompanyPos)
        txtMemo = view.findViewById(R.id.txtMemo)
        imgProfile = view.findViewById(R.id.imgProfile)
        btnClose = view.findViewById(R.id.btnClose)
        btnClose?.setOnClickListener { stopSelf() } // 기능 동일

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            // ▼ 위치만 수정: 가로 중앙 + 상단에서 살짝 아래(번호 밑)
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = statusBarHeight() + dp(160)   // 필요하면 140~200dp로 미세조정
        }

        root = view
        wm.addView(view, lp)
    }

    private fun bindData(card: CardCallInfoResponse?, phone: String?) {
        val nameStr = card?.name?.takeIf { it.isNotBlank() } ?: "이름 없음"
        val comp = card?.company?.takeIf { it.isNotBlank() }
        val pos  = card?.position?.takeIf { !it.isNullOrBlank() }
        val companyPos = listOfNotNull(comp, pos).joinToString(" · ")
            .ifBlank { phone ?: "회사/직책 없음" }
        val memoStr = card?.memoSummary?.takeIf { !it.isNullOrBlank() } ?: "메모 없음"

        txtName?.text = nameStr
        txtCompanyPos?.text = companyPos
        txtMemo?.text = memoStr

        val url = card?.cardImageForDisplay
        if (url.isNullOrBlank()) {
            imgProfile?.setImageResource(R.drawable.ic_person_placeholder)
        } else {
            Glide.with(this@IncomingOverlayService)
                .load(url)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(imgProfile!!)
        }
    }

    private fun startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(NOTI_CH, "Incoming overlay", NotificationManager.IMPORTANCE_MIN)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
        val noti = NotificationCompat.Builder(this, NOTI_CH)
            .setSmallIcon(R.drawable.ic_stat_call)
            .setOngoing(true)
            .setContentTitle("통화 정보 표시 중")
            .setContentText("전화 오버레이 활성화")
            .build()
        // FGS 타입 지정 안 함(권한 이슈 회피)
        startForeground(NOTI_ID, noti)
    }

    // ========== 유틸 ==========
    private fun statusBarHeight(): Int {
        val resId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) resources.getDimensionPixelSize(resId) else 0
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
