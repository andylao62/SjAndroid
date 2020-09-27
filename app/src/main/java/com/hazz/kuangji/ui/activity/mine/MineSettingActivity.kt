package com.hazz.kuangji.ui.activity.mine

import android.content.Intent
import androidx.appcompat.widget.Toolbar
import com.hazz.kuangji.Constants
import com.hazz.kuangji.R
import com.hazz.kuangji.base.BaseActivity
import com.hazz.kuangji.ui.activity.LoginActivity
import com.hazz.kuangji.utils.ActivityManager
import com.hazz.kuangji.utils.SPUtil
import com.hazz.kuangji.utils.ToolBarCustom
import com.hazz.kuangji.utils.Utils
import com.hazz.kuangji.widget.CommonDialog
import com.tencent.bugly.beta.Beta
import kotlinx.android.synthetic.main.activity_set.*
import kotlinx.android.synthetic.main.activity_set.tv_login
import kotlinx.android.synthetic.main.activity_set.tv_zijin
import kotlinx.android.synthetic.main.activity_invitefriends_record.mToolBar
import org.greenrobot.eventbus.EventBus
import skin.support.SkinCompatManager


class SettingActivity : BaseActivity() {


    var commonDialog: CommonDialog? =null;

    override fun layoutId(): Int {
        return R.layout.activity_set
    }

    override fun initData() {

    }


    override fun initView() {
        ToolBarCustom.newBuilder(mToolBar as Toolbar)
                .setTitle(getString(R.string.setting))
                .setOnLeftIconClickListener { finish() }

        tv_version.text="V"+Utils.getVersionName(this)

    }


    override fun start() {
        tv_login.setOnClickListener {
            startActivity(Intent(this, MineLoginPwdActivity::class.java))
        }
        tv_zijin.setOnClickListener {

            
            startActivity(Intent(this, MineExchangePwdActivity::class.java))
        }
        tv_logout.setOnClickListener {
            commonDialog=CommonDialog(this)
            commonDialog?.run {
                setContent("确认要退出登录吗？")
                setDialogClickListener(object : CommonDialog.DialogClickListener
                {
                    override fun ok() {
                        outLogin()
                    }
                    override fun cancel() {
                    }
                })
                builder()
            }
        }
        rl_version.setOnClickListener {
            Beta.checkAppUpgrade()
        }

        tv_skin.setOnClickListener {
            if (!SPUtil.getBoolean("skin"))
            {
                SPUtil.putBoolean("skin",true)
                SkinCompatManager.getInstance().loadSkin("night", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN); // 后缀加载
            }
            else
            {
                SPUtil.putBoolean("skin",false)
                SkinCompatManager.getInstance().restoreDefaultTheme();//恢复默认皮肤
            }
            EventBus.getDefault().post(Constants.CODE_IMAGE_BROAD)
        }

    }


    fun outLogin()
    {
        SPUtil.putString("token", "")
        SPUtil.removeObj("certification")
        startActivity(Intent(this, LoginActivity::class.java))
        ActivityManager.getInstance().finishOthers(LoginActivity::class.java)
    }

}
