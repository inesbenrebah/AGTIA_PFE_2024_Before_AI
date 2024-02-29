package com.example.agtia.Splash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.agtia.R

//lehne khdithina list mtaa items
class OnboardingItemsAdapter (private  val onboardingItem: List<OnboardingItem>):
RecyclerView.Adapter<OnboardingItemsAdapter.OnboardingItemsViewHolder>()


{
    inner class OnboardingItemsViewHolder(view : View):RecyclerView.ViewHolder(view){
//extends mn RecyclerView.ViewHolder khdhina el Ui elemnts  lel recyclerView
        private val imageOnboarding = view.findViewById<ImageView>(R.id.imageOnboarding)
        private val textTitle=view.findViewById<TextView>(R.id.textTitle)
        private val textDescrpition =view.findViewById<TextView>(R.id.textDescription)

//bind lehne connectina el data mte3na bl recyclerview
        fun bind (onboardingItem: OnboardingItem){
            imageOnboarding.setImageResource(onboardingItem.onboardingImage)
            textTitle.text=onboardingItem.title
            textDescrpition.text=onboardingItem.description
        }
    }
//onCreateViewHolder() elihne bch namel new view holder bch t3awena bch najmou nasn3ou object (instance)
//  bc nrepresentiw el appearnce mta3 el item el we7d fi recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingItemsViewHolder {
return OnboardingItemsViewHolder(
    LayoutInflater.from(parent.context).inflate(
        R.layout.onboarding_item_container,parent,false
    )
)
    }
//lehna bch nchoufo kadeh fama mn item fl recyclerview
    override fun getItemCount(): Int {
        return onboardingItem.size
    }
//onBindViewHolder() is like a manager telling each helper friend (ViewHolder)
// how to look based on the specific item's data at a particular position in the list. The helper friend (ViewHolder)
// then uses its bind() function to set itself up with the right picture and words for that item.
    override fun onBindViewHolder(holder: OnboardingItemsViewHolder, position: Int) {
 holder.bind(onboardingItem[position])    }
}