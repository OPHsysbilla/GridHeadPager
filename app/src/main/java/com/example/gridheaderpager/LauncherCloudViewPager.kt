package com.example.gridheaderpager

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.yuanfudao.android.megrez.laucher.vm.api.cloud.PropFileData
import com.yuanfudao.android.megrez.laucher.vm.api.cloud.getId


/**
 * created by tangjing on 2021/8/2
 */
class LauncherCloudViewPager @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    typeStyle: Int = 0
) : FrameLayout(context, attributeSet, typeStyle) {
    private val viewList = mutableListOf<View>()
    private val pageList = mutableListOf<List<CloudFile>>()

    // TODO： @leijialin 改成数据驱动的形式
    var needShowSize = false
    var disableSelect = false

    private val maxCountOnePage: Int = 18

    private var pageHash = 0

    var currentPage = -1
        set(value) {
            if (value >= 0 && value < pageList.size) {
                field = value
                render(value)
            }
        }
    var isSelectMode = false

    fun bindPageHash(hash: Int) {
        pageHash = hash
    }

    private fun updateStatus(isSelectMode: Boolean) {
        this.isSelectMode = isSelectMode
        if (!isSelectMode) {
            pageList.flatten().forEach {
                it.isSelected = false
            }
        }
        render(currentPage)
    }

    fun selectAll(isSelected: Boolean) {
        pageList.flatten().forEach {
            it.isSelected = isSelected
        }
        render(currentPage)
    }

    fun setData(
        cloudFiles: List<CloudFile>,
        lastScanId: String? = null,
    ) {
        pageList.clear()
        pageList.addAll(cloudFiles.chunked(maxCountOnePage))
        val pageIndex = cloudFiles.indexOfFirst { it.prop.getId() == lastScanId }
        currentPage = (pageIndex.coerceAtLeast(0)) / maxCountOnePage
    }

    fun refreshFileStatus(id: String, status: PropExists?, localPath: String?) {
        pageList.forEachIndexed { pageIndex, onePage ->
            onePage.find { model ->
                model.prop.getId() == id
            }?.let { model ->
                model.prop.localPath = localPath
                model.prop.existState = status ?: PropExists.Cloud
                val itemView = viewList.getOrNull(onePage.indexOf(model))
                innerRenderIconEach(itemView, model)
            }
        }
    }

    private fun innerRenderIconEach(itemView: View?, data: CloudFile) {
        itemView ?: return
        val resId = data.getStatus(data.prop.existState)
        val icon = itemView.findViewById<ImageView>(R.id.prop_file_loading)
        icon?.isVisible = resId != null
        icon?.setImageResource(resId ?: 0)
    }

    @SuppressLint("SetTextI18n")
    private fun render(pageIndex: Int) {
        val page = pageList.getOrNull(pageIndex) ?: return
        val count = page.size - viewList.size
        prepareView(count)
        val pairs = page.zip(viewList)
        pairs.forEach { (file, view) ->
            val detail = file.prop.fileDetail
            view.visibility = VISIBLE
            view.setOnClickListener(null)
            view.findViewById<ImageView>(R.id.prop_file_icon).setImageResource(file.getIcon())
            innerRenderIconEach(view, file)
            view.findViewById<TextView>(R.id.prop_file_name).apply {
                this.text = detail.name
            }
            val tvSize = view.findViewById<TextView>(R.id.prop_file_size)
            tvSize.isVisible = needShowSize
            view.findViewById<TextView>(R.id.tv_expire_tag).let {
                val expireDays = file.prop.fileDetail.expiredDays
                it.text = "${expireDays}天后过期"
                it.visibility = if (expireDays > 0) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            val selectView = view.findViewById<ImageView>(R.id.iv_select)
            renderSelectView(selectView, file)
            view.onClick {
                if (isSelectMode) {
                    file.isSelected = !file.isSelected
                }
                renderSelectView(selectView, file)
                file.onClick(isSelectMode)
            }
            view.setOnLongClickListener {
                if (disableSelect) return@setOnLongClickListener false
                if (!isSelectMode) {
                    file.isSelected = true
                    file.onLongClick(file.isSelected)
                    true
                } else {
                    false
                }
            }

        }
    }

    private fun renderSelectView(selectView: ImageView, file: CloudFile) {
        if (isSelectMode) {
            selectView.visibility = VISIBLE
            val selectIcon = if (file.isSelected) {
                R.mipmap.megrez_view_ic_circle_select
            } else {
                R.mipmap.megrez_view_ic_circle_unselect
            }
            selectView.setImageResource(selectIcon)
        } else {
            selectView.visibility = INVISIBLE
        }
    }

    private fun prepareView(count: Int) {
        val oldSize = viewList.size
        val newViews = mutableListOf<View>()
        when {
            count > 0 -> {
                val inflater = LayoutInflater.from(context)
                repeat(count) {
                    newViews.add(
                        inflater.inflate(
                            R.layout.launcher_display_prop_file_item,
                            null,
                            false
                        )
                    )
                }
                viewList.addAll(newViews)
            }
            count < 0 -> {
                viewList.takeLast(-count).forEach {
                    it.isVisible = false
                }
            }
            else -> {
            }
        }
        newViews.forEachIndexed { index, view ->
            val offsetX = (oldSize + index).rem(3)
            val offsetY = (oldSize + index).div(3)
            val lp = LayoutParams(
                context.dp2px(400),
                context.dimen2px(R.dimen.launcher_cloud_file_item_height)
            ).apply {
                setMargins(
                    context.dp2px(30) + offsetX * context.dp2px(400),
                    offsetY * context.dp2px(184),
                    0,
                    0
                )
            }
            addView(view, lp)
        }
    }

    fun getPageList(): List<List<CloudFile>> = pageList

    fun getSelectedFile() = pageList.flatten().filter { it.isSelected }

    fun getSelectedFileIds() = getSelectedFile().map { it.prop.fileDetail.id }

    fun getSelectedFileMd5s() = getSelectedFile().mapNotNull { it.prop.fileDetail.md5 }

    data class CloudFile constructor(
        val prop: PropFileData,
        val onClick: (Boolean) -> Unit,
        val onLongClick: (Boolean) -> Unit = {}
    ) {
        var isSelected = false

        fun getIcon(): Int {
            return when (prop.fileDetail.format) {
                "folder" -> R.drawable.launcher_icon_folder
                "pdf" -> R.drawable.launcher_icon_pdf
                "doc", "docx" -> R.drawable.launcher_icon_word
                "xls", "xlsx" -> R.drawable.launcher_icon_excel
                "ppt", "pptx" -> R.drawable.launcher_icon_ppt
                "txt" -> R.drawable.launcher_icon_txt
                "jpeg", "jpg", "png" -> R.drawable.launcher_icon_pic
                else -> R.drawable.launcher_icon_unknown
            }
        }

        fun getStatus(status: PropExists? = prop.existState): Int? {
            return when (status) {
                PropExists.Folder -> null
                PropExists.Downloading -> R.drawable.launcher_file_status_download
                PropExists.Cloud -> R.drawable.launcher_file_status_in_cloud
                else -> null
            }
        }
    }
}