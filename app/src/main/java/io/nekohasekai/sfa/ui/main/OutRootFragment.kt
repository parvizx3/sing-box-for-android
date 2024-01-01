package io.nekohasekai.sfa.ui.main

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColor
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import io.nekohasekai.libbox.Libbox
import io.nekohasekai.sfa.R
import io.nekohasekai.sfa.bg.BoxService
import io.nekohasekai.sfa.constant.Status
import io.nekohasekai.sfa.database.GlobalVariables
import io.nekohasekai.sfa.database.ProfileManager
import io.nekohasekai.sfa.database.Settings
import io.nekohasekai.sfa.databinding.FragmentDashboardBinding
import io.nekohasekai.sfa.databinding.FragmentOutRootBinding
import io.nekohasekai.sfa.ktx.errorDialogBuilder
import io.nekohasekai.sfa.ui.MainActivity
import io.nekohasekai.sfa.ui.dashboard.GroupsFragment
import io.nekohasekai.sfa.ui.dashboard.OverviewFragment
import io.nekohasekai.sfa.utils.CommandClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutRootFragment : Fragment(R.layout.fragment_out_root) {

    private val activity: MainActivity? get() = super.getActivity() as MainActivity?
    private var binding: FragmentOutRootBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentOutRootBinding.inflate(inflater, container, false)
        this.binding = binding
        onCreate()
        return binding.root
    }
    private fun onCreate() {
        val activity = activity ?: return
        val binding = binding ?: return
        val density = resources.displayMetrics.density

        binding.imageView3.setOnClickListener {
            activity.findNavController(R.id.nav_host_fragment_activity_my).navigate(R.id.navigation_settings)
        }

        binding.imageView1.isClickable = false
        binding.imageView1.setOnClickListener {
            activity.findNavController(R.id.nav_host_fragment_activity_my).navigate(R.id.navigation_groups)
        }

        activity.serviceStatus.observe(viewLifecycleOwner) {
            when (it) {
                Status.Stopped -> {

                    binding.imageView1.isClickable = false
                }

                Status.Starting -> {

                }

                Status.Started -> {
                    binding.locationText.text = GlobalVariables.location
                    binding.imageView1.isClickable = true
                    binding.statusText.text = "Connected"
                    // Move to the right and change color
                    ObjectAnimator.ofFloat(binding.ellipse, "translationX", density * 130).apply {
                        duration = 500
                        start()
                    }

                    // Color animation to change color
                    ValueAnimator.ofArgb(
                        Color.parseColor("#ffffff"), // Original color
                        Color.parseColor("#2B73FF") // New color
                    ).apply {
                        duration = 500
                        addUpdateListener { animator -> binding.ellipse.setColorFilter(animator.animatedValue as Int) }
                        start()
                    }
                }

                Status.Stopping -> {

                }

                else -> {}
            }
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                runCatching {
                    val gg = ProfileManager.list()[0]
                    Settings.selectedProfile  = gg.id
                }
            }
        }

        binding.ellipse.setOnClickListener {
                when (activity.serviceStatus.value) {

                    Status.Stopped -> {
                        activity.startService()

                        binding.statusText.text = "Connected"
                        // Move to the right and change color
                        ObjectAnimator.ofFloat(binding.ellipse, "translationX", density * 130).apply {
                            duration = 500
                            start()
                        }

                        // Color animation to change color
                        ValueAnimator.ofArgb(
                            Color.parseColor("#ffffff"), // Original color
                            Color.parseColor("#2B73FF") // New color
                        ).apply {
                            duration = 500
                            addUpdateListener { animator -> binding.ellipse.setColorFilter(animator.animatedValue as Int) }
                            start()
                        }
                        binding.imageView1.isClickable = true
                    }

                    Status.Started -> {
                        BoxService.stop()

                        binding.statusText.text = "Disconnected"
                        // Move back to original position and revert color
                        ObjectAnimator.ofFloat(binding.ellipse, "translationX", density * 20).apply {
                            duration = 500
                            start()
                        }

                        // Color animation to revert color
                        ValueAnimator.ofArgb(
                            Color.parseColor("#F03A50"),
                            Color.parseColor("#ffffff") // Original color
                        ).apply {
                            duration = 500
                            addUpdateListener { animator -> binding.ellipse.setColorFilter(animator.animatedValue as Int) }
                            start()
                        }
                        GlobalVariables.location = "Best Location"
                        binding.locationText.text = "Best Location"
                    }

                    else -> {}
                }

        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}