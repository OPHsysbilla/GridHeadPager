package com.example.gridheaderpager


/**
 * Created by lei.jialin on 2021/7/27
 */
data class PropFileDetail(
    val id: String,
    val name: String,
    val type: Int,
    val format: String?,
    val ossId: String?,
    val md5: String?,
    val parentId: String,
    val createdTime: Long,
    val updatedTime: Long,
    val length: Long,
    val expiredTime: Long,
    val status: Int = 0, // 新加的参数需要赋初值
) {
    var scanChildId: String? = null
    var expiredDays: Long = 0
}

fun PropFileDetail.isInRecycleBin() = this.status == 1
fun PropFileDetail.isDestroyed() = this.status == 2
fun PropFileDetail.isStateNotAvailable() = this.isInRecycleBin() || this.isDestroyed()
fun PropFileDetail.isInMyFile() = this.status == 0
fun PropFileDetail.isFolderDetail() = this.type == 0