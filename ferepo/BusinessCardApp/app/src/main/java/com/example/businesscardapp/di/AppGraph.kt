package com.example.businesscardapp.di

import com.example.businesscardapp.data.network.Repository

object AppGraph {
    // 필요 시 여기서 다른 레포/유즈케이스도 lazy로 만들어 확장 가능
    val repository: Repository by lazy { Repository() }
}
