package com.yuanfudao.android.megrez.laucher.vm.api.cloud

import com.example.gridheaderpager.PropExists
import com.example.gridheaderpager.PropFileDetail
import com.example.gridheaderpager.isFolderDetail

/**
 * Created by lei.jialin on 2021/7/27
 */
class PropFileData(
    val fileDetail: PropFileDetail,
    var localPath: String?,
    var existState: PropExists,
)

fun PropFileData.isFolder() = this.fileDetail.isFolderDetail()
fun PropFileData.getId() = this.fileDetail.id
fun PropFileData.isCloudFile() = this.existState == PropExists.Cloud
fun PropFileData.isIntactLocal() = this.existState == PropExists.IntactLocal
fun PropFileData.isDownloading() = this.existState == PropExists.Downloading

data class PropUpdateBean(
    val fileDetail: PropFileDetail,
    val localPath: String?,
    val existState: PropExists,
    val isSuccess: Boolean =  true,
    val errorToast: String? = "",
)