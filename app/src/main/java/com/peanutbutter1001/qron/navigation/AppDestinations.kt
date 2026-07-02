package com.peanutbutter1001.qron.navigation

import kotlinx.serialization.Serializable

/**
 * 타입 안전(type-safe) 네비게이션 라우트 정의.
 * NavHost 및 하단탭에서만 사용되며, feature 모듈은 이 타입을 알 필요가 없다
 * (feature Route는 순수 콜백만 받는다).
 */
@Serializable
data object Scanner

@Serializable
data object History

@Serializable
data class Result(val id: Long)
