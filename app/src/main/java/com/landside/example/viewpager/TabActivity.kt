package com.landside.example.viewpager

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.landside.example.R
import com.landside.example.share.Share
import com.landside.example.share.Share.ShareItem
import com.landside.example.viewpager.TabContract.AttachActivityView
import com.landside.shadowstate.ShadowState
import com.landside.shadowstate_annotation.AttachState
import com.landside.shadowstate_annotation.BindState
import com.landside.shadowstate_annotation.ShareState
import kotlinx.android.synthetic.main.activity_tab.tab
import kotlinx.android.synthetic.main.activity_tab.tv_attach_age
import kotlinx.android.synthetic.main.activity_tab.tv_attach_name
import kotlinx.android.synthetic.main.activity_tab.tv_share_count
import kotlinx.android.synthetic.main.activity_tab.tv_share_item
import kotlinx.android.synthetic.main.activity_tab.tv_share_name
import kotlinx.android.synthetic.main.activity_tab.viewpager

@BindState(state = Tab::class, agent = TabAgent::class)
@ShareState(states = [Share::class],agent = [MainTagShareAgent::class])
@AttachState(states = [AttachInfo::class,AttachInfo2::class],agents = [ActivityAttachAgent::class,ActivityAttachAgent2::class])
class TabActivity : AppCompatActivity(),TabContract.MainTabView , AttachActivityView {

  val fragments = listOf<Fragment>(
      TabFragment1(),
      TabFragment1(),
      TabFragment2()
  )

  val titles = listOf(
      "tab1","tab2","tab3"
  )

  lateinit var presenter: TabPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ShadowState.bind(this)

    setContentView(R.layout.activity_tab)
    viewpager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
      override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
      }

      override fun getItem(position: Int): Fragment = fragments[position]

      override fun getCount(): Int = fragments.size

    }
    tab.setupWithViewPager(viewpager)
    presenter = TabPresenter(this)
  }

  fun change(view: View){
    presenter.changeName()
  }

  override fun setShareName(name: String) {
    tv_share_name.text = "tab activity: $name"
  }

  override fun setShareCount(count: Int) {
    tv_share_count.text ="tab activity:$count"
  }

  override fun setShareItem(item: ShareItem<String>) {
    tv_share_item.text = "tab activity: $item"
  }

  override fun setAttachName(name: String) {
    tv_attach_name.text = "当前页面附加状态的名字：${name}"
  }

  override fun setAttachAge(age: Int) {
    tv_attach_age.text = "年龄：${age}"
  }
}