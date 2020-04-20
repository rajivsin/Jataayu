package `in`.projecteka.jataayu.consent.ui.fragment

import `in`.projecteka.jataayu.consent.databinding.FragmentConsentHostBinding
import `in`.projecteka.jataayu.consent.ui.adapter.ConsentPagerAdapter
import `in`.projecteka.jataayu.consent.viewmodel.ConsentHostFragmentViewModel
import `in`.projecteka.jataayu.core.model.MessageEventType
import `in`.projecteka.jataayu.presentation.ui.fragment.BaseFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_consent.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ConsentHostFragment : BaseFragment() {

    private lateinit var binding: FragmentConsentHostBinding

    private val eventBusInstance = EventBus.getDefault()

    private val viewModel: ConsentHostFragmentViewModel by sharedViewModel()

    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout

    companion object {
        fun newInstance() = ConsentHostFragment()
    }

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            view_pager.adapter?.notifyDataSetChanged()
        }

        override fun onPageScrollStateChanged(state: Int) {
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentConsentHostBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        initialViewpagerSetup()
        initObservers()
    }

    private fun initialViewpagerSetup() {
        viewPager = binding.viewPager
        viewPager.adapter = ConsentPagerAdapter(context!!, childFragmentManager)
        tabs = binding.tabs
        tabs.setupWithViewPager(viewPager)
        view_pager.addOnPageChangeListener(onPageChangeListener)
    }

    private fun initObservers() {
        viewModel.viewPagerState.observe(this, Observer {
            when (it) {
                ConsentHostFragmentViewModel.Action.SELECT_CONSENTS_TAB -> {
                    activity?.runOnUiThread {
                        binding.viewPager.currentItem = 1
                    }
                }
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun onTabPositionReceived(messageEventType: MessageEventType) {
        if (messageEventType == MessageEventType.SELECT_CONSENTS_TAB) {
            viewModel.selectConsentsTab()
        }
        eventBusInstance.removeStickyEvent(MessageEventType::class.java)
    }

    override fun onStart() {
        super.onStart()
        if (!eventBusInstance.isRegistered(this))
            eventBusInstance.register(this)
    }

    override fun onDestroy() {
        eventBusInstance.unregister(this)
        super.onDestroy()
    }
}


