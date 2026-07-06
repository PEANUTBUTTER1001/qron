package com.peanutbutter1001.qron.core.vision

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 스캔 직후 결과 화면에서만 사용하는 QR crop 이미지 인메모리 캐시.
 * - DB 저장 없이 resultId → Bitmap 매핑을 메모리에만 유지한다.
 * - 결과 화면이 닫힐 때 evict()를 호출해 Bitmap을 즉시 recycle한다.
 * - @Singleton 이므로 스캐너와 결과 화면이 동일 인스턴스를 공유한다.
 */
@Singleton
class QRImageCache @Inject constructor() {
    private val cache = mutableMapOf<Long, Bitmap>()

    fun put(id: Long, bitmap: Bitmap) {
        cache[id] = bitmap
    }

    fun get(id: Long): Bitmap? = cache[id]

    /** 캐시에서 제거하고 Bitmap 메모리를 즉시 해제한다. */
    fun evict(id: Long) {
        cache.remove(id)?.recycle()
    }
}
