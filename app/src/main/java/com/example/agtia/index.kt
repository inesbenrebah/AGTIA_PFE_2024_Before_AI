package com.example.agtia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import java.text.FieldPosition

class index : AppCompatActivity() {
    private lateinit var onboardingItemsAdapter: OnboardingItemsAdapter
    private lateinit var indicatorContainer :LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
        setOnboardingItems()
        setupindicators()

    }
    private fun  navigateToHomeActivity (){
        startActivity(Intent(applicationContext,HomeActivity::class.java))
        finish()
    }
    private fun setOnboardingItems(){
        onboardingItemsAdapter= OnboardingItemsAdapter(
            listOf(
                OnboardingItem(
                    onboardingImage = R.drawable.picture1,
                    title = "Manage Your Task",
                    description = "Organize all your to do's and projects .Color tag them to set priorities and catefories"
                ),
                OnboardingItem(
                    onboardingImage = R.drawable.picture2,
                    title = "Work On Time ",
                    description = "When you're overwhelmed by the amount of work you have on your plate, stop and rethink ."

                ),
                OnboardingItem(
                    onboardingImage = R.drawable.picture3,
                    title = "Get Reminder On Time  ",
                    description = "When youencounter a small task less than 5 minutes to complete ."

                )
            )
        )
        val onboardingViewPager=findViewById<ViewPager2>(R.id.onboardingViewPager)
        onboardingViewPager.adapter=onboardingItemsAdapter
        onboardingViewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
            setCurrentIndicator(position)
            }
        })
        (onboardingViewPager.getChildAt(0)as RecyclerView).overScrollMode=
            RecyclerView.OVER_SCROLL_NEVER

        //lehne khadamt el skip wel btn w flesh ykadem bka3ba
        findViewById<ImageView>(R.id.imageNext).setOnClickListener {
            if (onboardingViewPager.currentItem +1 < onboardingItemsAdapter.itemCount){
                onboardingViewPager.currentItem +=1
            }else {
                navigateToHomeActivity()
            }
        }
        findViewById<TextView>(R.id.textSkip).setOnClickListener {
            navigateToHomeActivity()
        }
        findViewById<Button>(R.id.BtnGetStarted).setOnClickListener {
            navigateToHomeActivity()
        }

    }
//lehne amalt aka les 3 points eli mn fouk bch kol matetbade el page tetbdel el position taa el nokta
private fun setupindicators(){
    indicatorContainer=findViewById(R.id.indicatorsContainer)
    val indicator = arrayOfNulls<ImageView>(onboardingItemsAdapter.itemCount)
    val layoutParams :LinearLayout.LayoutParams=
        LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    layoutParams.setMargins(8,0,8,0)
    for (i in indicator.indices){
        indicator[i] = ImageView(applicationContext)
        indicator[i]?.let {
            it.setImageDrawable(
                ContextCompat.getDrawable(
                   applicationContext,
                    R.drawable.indicator_inactive_background
                )
            )
            it.layoutParams= layoutParams
            indicatorContainer.addView(it)
        }
    }
}
    //wlehne taba3na bl position lel indicator
private  fun setCurrentIndicator(position: Int){
    val childCount = indicatorContainer.childCount
    for (i in  0 until childCount){
        val imageView = indicatorContainer.getChildAt(i) as ImageView
        if (i == position){
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.indicator_active_background
                    ))
        }else{
            imageView.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.indicator_inactive_background
                )
            )
        }
    }

}
}
