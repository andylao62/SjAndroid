package com.hazz.kuangji.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.text.SpannableString
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.hazz.kuangji.Constants
import com.hazz.kuangji.R
import com.hazz.kuangji.base.BaseFragment
import com.hazz.kuangji.mvp.contract.IContractView
import com.hazz.kuangji.mvp.model.Certification
import com.hazz.kuangji.mvp.model.Home
import com.hazz.kuangji.mvp.model.Msg
import com.hazz.kuangji.mvp.presenter.CertificationInfoPresenter
import com.hazz.kuangji.mvp.presenter.HomePresenter
import com.hazz.kuangji.mvp.presenter.MsgPresenter
import com.hazz.kuangji.ui.activity.MainActivity
import com.hazz.kuangji.ui.activity.asset.ChargeActivity
import com.hazz.kuangji.ui.activity.asset.ExtractCoinActivity
import com.hazz.kuangji.ui.activity.asset.TransferCoinActivity
import com.hazz.kuangji.ui.activity.home.*
import com.hazz.kuangji.ui.activity.mine.InviteActivity
import com.hazz.kuangji.ui.activity.mine.MineCertificationActivity
import com.hazz.kuangji.ui.adapter.HomeAdapter
import com.hazz.kuangji.utils.DensityUtils
import com.hazz.kuangji.utils.DisplayManager
import com.hazz.kuangji.utils.SPUtil
import com.hazz.kuangji.utils.SToast
import com.hazz.kuangji.widget.RewardItemDeco
import com.scwang.smartrefresh.layout.util.DensityUtil
import com.youth.banner.BannerConfig
import com.youth.banner.Transformer
import com.youth.banner.loader.ImageLoader
import kotlinx.android.synthetic.main.fragment_new_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class HomeFragment : BaseFragment(), IContractView.HomeView, IContractView.MsgView, IContractView.ICertificationInfoView {

    private var adList: MutableList<String>? = mutableListOf()
    private var mAdapter: HomeAdapter? = null
    private var mHomePresenter: HomePresenter = HomePresenter(this)
    private var mCoinPresenter: MsgPresenter = MsgPresenter(this)
    private val mCertificationInfoPresenter = CertificationInfoPresenter(this)
    private var mCertification: Certification? = null

    override fun getCertification(certification: Certification) {
        mCertification = certification
        if (certification.status == 1) {
            SPUtil.putObj("certification", certification)
        }
    }

    override fun getMsg(rows: List<Msg>) {
        if (mView==null||activity==null)return
        if (!rows.isNullOrEmpty()) {
            var listInfo=ArrayList<CharSequence>()
            for (s in rows)
            {
                listInfo.add(SpannableString(s.title))
            }
            marqueeView.startWithList(listInfo)
            marqueeView.setOnItemClickListener { position, textView ->
                val item = rows[position]
                val intent = Intent(activity, MsgDescActivity::class.java)
                intent.putExtra("message", item)
                startActivity(intent)
            }
        }
    }

    override fun getHome(msg: Home) {
        if (mView==null||activity==null)return
        sl_refresh?.isRefreshing=false

        val carousel = msg.carousel
        if (!carousel.isNullOrEmpty()) {
            adList?.clear()
            for (a in carousel) {
                adList?.add(a.url)
            }
            initBanner(adList!!)
        }
        mAdapter?.setNewData(msg.products)
    }

    override fun zuyongSucceed(msg: String) {
    }


    override fun getLayoutId(): Int {
        return R.layout.fragment_new_home
    }


    override fun initView() {
        EventBus.getDefault().register(this)
        mCertification = SPUtil.getObj("certification", Certification::class.java)

        var layoutParams: RelativeLayout.LayoutParams= toolbar.layoutParams as RelativeLayout.LayoutParams
        layoutParams.topMargin= activity?.let { DensityUtils.getStatusBarHeight(it) }!!
        toolbar.layoutParams=layoutParams

        sl_refresh=activity?.findViewById(R.id.sl_refresh)
        sl_refresh?.isRefreshing=true
        sl_refresh?.setColorSchemeResources(R.color.color_main)
        sl_refresh?.setOnRefreshListener {
            lazyLoad()
        }

        recycle_view.layoutManager = LinearLayoutManager(activity)//创建布局管理
        mAdapter = HomeAdapter(R.layout.item_home, null)
        recycle_view.adapter = mAdapter
        mAdapter?.bindToRecyclerView(recycle_view)
        mAdapter?.setEmptyView(R.layout.fragment_empty)
        val leftRightOffset = DensityUtil.dp2px(15f)
        recycle_view.addItemDecoration(RewardItemDeco(0, 0, 0, leftRightOffset, 0))

        iv_mine.setOnClickListener {
            (activity as MainActivity).openMine()
        }
        iv_msg.setOnClickListener {
            startActivity(Intent(activity, MsgListActivity::class.java))
        }

        ll_buy.setOnClickListener {
            startActivity(Intent(activity, ExchangeBuyActivity::class.java))
        }
        ll_sale.setOnClickListener {
            if (isCertificated())
                startActivity(Intent(activity, ExchangeSaleActivity::class.java))
        }
        ll_transfer.setOnClickListener {
            if (isCertificated())
                startActivity(Intent(activity, TransferCoinActivity::class.java))
        }

        iv_invite.setOnClickListener {
            startActivity(Intent(activity, InviteActivity::class.java))
        }

        ll_recharge.setOnClickListener {
            startActivity(Intent(activity, ChargeActivity::class.java))
        }
        ll_extract.setOnClickListener {
            if (isCertificated())
                startActivity(Intent(activity, ExtractCoinActivity::class.java))
        }
        ll_exchange.setOnClickListener {
            if (isCertificated())
                startActivity(Intent(activity, ExchangeCoinActivity::class.java))
        }
    }

    override fun lazyLoad() {
        setImage()
        mCoinPresenter.getMsg()
        mHomePresenter.getHome()
        mCertificationInfoPresenter.getCertification()
    }

    /**
     * 设置头像
     */
    private fun setImage()
    {
        val requestOptions=RequestOptions.bitmapTransform(CircleCrop()).error(R.mipmap.icon_home_mine)
        Glide.with(activity!!).load(Constants.URL_INVITE+SPUtil.getString("image")).apply(requestOptions).into(iv_mine)
    }

    /**
     * 判断是否已经实名认证
     */
    private fun isCertificated():Boolean
    {
        when (mCertification?.status) {
            0 -> {
                SToast.showText("实名认证审核中，请稍等")
                return false  
            }
            1 -> {
                return true
            }
            else -> {
                SToast.showText("尚未实名认证，请先前往实名认证")
                Handler().postDelayed(Runnable {
                    startActivity(Intent(activity, MineCertificationActivity::class.java))
                }, 500)
                return false
            }
        }
    }

    private fun initBanner(list: List<String>) {
        banner?.run {
            setImageLoader(GlideImageLoader())
            var layoutParams1: ViewGroup.LayoutParams = banner.layoutParams
            layoutParams1.height = (DisplayManager.getScreenWidth()?.times(175))?.div(372)!!
            layoutParams = layoutParams1;
            setImages(list)
            //设置banner动画效果
            setBannerAnimation(Transformer.DepthPage)
            //设置自动轮播，默认为true
            isAutoPlay(true)
            //设置轮播时间
            setDelayTime(3000)
            //设置指示器位置（当banner模式中有指示器时）
            setIndicatorGravity(BannerConfig.CENTER)
            //banner设置方法全部调用完毕时最后调用
            start()
        }
    }

    private class GlideImageLoader : ImageLoader() {
        override fun displayImage(context: Context, path: Any, imageView: ImageView) {
            Glide.with(context).load(path).into(imageView)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: String) {
        if (event == Constants.CODE_CERTIFICATION_BROAD) {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    if (mCertification?.status!=1)
                    {
                        mCertificationInfoPresenter.getCertification()
                    }
                }
            } , 0, 3*60*1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            mCertification = SPUtil.getObj("certification", Certification::class.java)
        }
    }


}
