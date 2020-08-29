package com.hazz.kuangji.ui.activity.mine

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.widget.Toolbar
import com.hazz.kuangji.Constants
import com.hazz.kuangji.R
import com.hazz.kuangji.base.BaseActivity
import com.hazz.kuangji.utils.ImageUtlis
import com.hazz.kuangji.utils.SToast
import com.hazz.kuangji.utils.ToolBarCustom
import kotlinx.android.synthetic.main.activity_charge.*
import kotlinx.android.synthetic.main.activity_contact.*
import kotlinx.android.synthetic.main.activity_contact.tv_down
import kotlinx.android.synthetic.main.activity_download.*
import kotlinx.android.synthetic.main.activity_download.tv_address
import kotlinx.android.synthetic.main.activity_rule.mToolBar

class MineDownloadActivity : BaseActivity()
{

    private var mCodeBitmap:Bitmap?=null

    override fun layoutId(): Int {
        return R.layout.activity_download
    }

    override fun initData() {

    }

    override fun initView() {

        ToolBarCustom.newBuilder(mToolBar as Toolbar)
                .setTitle("下载地址")
                .setToolBarBgRescource(R.color.color_white)
                .setOnLeftIconClickListener { finish() }

        tv_address.text=Constants.URL_DOWNLOAD
        tv_address.setOnClickListener {

            val cm = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clipData = ClipData.newPlainText("invitation_code",tv_address.text.toString())

            cm.primaryClip = clipData

            SToast.showText("已成功复制充值地址")

        }

    }

    override fun start() {
        mCodeBitmap=BitmapFactory.decodeResource(resources,R.mipmap.icon_mine_download_qrcode)
        tv_down.setOnClickListener {
            permissionsnew!!.request(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE

            ).subscribe { permission ->
                if (permission!!) {

                    SToast.showText("图片保存成功")

                    ImageUtlis.saveBmp2Gallery(this,mCodeBitmap, "downloadCode")

                } else {
                    showMissingPermissionDialog()
                }
            }
        }

    }

}