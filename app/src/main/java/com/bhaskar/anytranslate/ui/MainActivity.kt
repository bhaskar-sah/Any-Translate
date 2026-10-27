package com.bhaskar.anytranslate.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bhaskar.anytranslate.R
import com.bhaskar.anytranslate.data.ml.ModelDownloadState
import com.bhaskar.anytranslate.data.model.CombinedTranslation
import com.bhaskar.anytranslate.data.model.Language
import com.bhaskar.anytranslate.data.model.SingleTranslation
import com.bhaskar.anytranslate.databinding.ActivityMainBinding
import com.bhaskar.anytranslate.databinding.BottomSheetHistoryBinding
import com.bhaskar.anytranslate.databinding.ItemTranslationCardBinding
import com.bhaskar.anytranslate.ui.adapters.HistoryAdapter
import com.bhaskar.anytranslate.utils.AdMobHelper
import com.bhaskar.anytranslate.utils.TextToSpeechHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var ttsHelper: TextToSpeechHelper
    private lateinit var adMobHelper: AdMobHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ttsHelper = TextToSpeechHelper(this)
        adMobHelper = AdMobHelper(this)

        setupAdMobBanner()
        setupSearchInput()
        setupLanguageChips()
        setupHeaderActions()
        observeViewModel()
    }

    private fun setupAdMobBanner() {
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun setupSearchInput() {
        binding.btnTranslate.setOnClickListener {
            performTranslation()
        }

        binding.etSearchQuery.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performTranslation()
                true
            } else {
                false
            }
        }
    }

    private fun performTranslation() {
        val query = binding.etSearchQuery.text?.toString().orEmpty()
        hideKeyboard()
        viewModel.translate(query)
        adMobHelper.onTranslationPerformed(this)
    }

    private fun setupLanguageChips() {
        binding.chipGroupLanguages.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                checkedIds.contains(R.id.chipGerman) -> viewModel.setFilter(SelectedLanguageFilter.GERMAN)
                checkedIds.contains(R.id.chipNepali) -> viewModel.setFilter(SelectedLanguageFilter.NEPALI)
                checkedIds.contains(R.id.chipHindi) -> viewModel.setFilter(SelectedLanguageFilter.HINDI)
                else -> viewModel.setFilter(SelectedLanguageFilter.ALL)
            }
        }
    }

    private fun setupHeaderActions() {
        binding.btnOpenHistory.setOnClickListener {
            showHistoryBottomSheet(isFavoritesOnly = false)
        }

        binding.btnOpenFavorites.setOnClickListener {
            showHistoryBottomSheet(isFavoritesOnly = true)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoading.collectLatest { loading ->
                        binding.progressBar.isVisible = loading
                        binding.btnTranslate.text = if (loading) "" else getString(R.string.translate_button)
                        binding.btnTranslate.isEnabled = !loading
                    }
                }

                launch {
                    viewModel.selectedFilter.collectLatest { filter ->
                        applyFilterVisibility(filter)
                    }
                }

                launch {
                    viewModel.modelDownloadState.collectLatest { state ->
                        when (state) {
                            is ModelDownloadState.Downloading -> {
                                binding.tvModelStatus.text = getString(R.string.model_downloading)
                                binding.tvModelStatus.isVisible = true
                            }
                            is ModelDownloadState.Ready -> {
                                binding.tvModelStatus.text = getString(R.string.model_ready)
                                binding.tvModelStatus.isVisible = true
                            }
                            is ModelDownloadState.Error -> {
                                binding.tvModelStatus.text = getString(R.string.model_failed)
                                binding.tvModelStatus.isVisible = true
                            }
                            is ModelDownloadState.Idle -> {
                                binding.tvModelStatus.isVisible = false
                            }
                        }
                    }
                }

                launch {
                    viewModel.currentTranslation.collectLatest { translation ->
                        if (translation != null) {
                            binding.layoutEmptyState.isVisible = false
                            binding.layoutResults.isVisible = true
                            bindTranslationResult(translation)
                        } else {
                            binding.layoutEmptyState.isVisible = true
                            binding.layoutResults.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.errorMessage.collectLatest { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun bindTranslationResult(result: CombinedTranslation) {
        bindCard(binding.cardGerman, result.german, result.query)
        bindCard(binding.cardNepali, result.nepali, result.query)
        bindCard(binding.cardHindi, result.hindi, result.query)
    }

    private fun bindCard(
        cardBinding: ItemTranslationCardBinding,
        translation: SingleTranslation,
        query: String
    ) {
        cardBinding.tvLanguageBadge.text = translation.language.flag
        cardBinding.tvLanguageName.text = translation.language.displayName
        cardBinding.tvSourceBadge.text = translation.source.label
        cardBinding.tvTranslatedWord.text = translation.translatedText

        // Phonetic or part of speech
        val phoneticInfo = buildString {
            if (!translation.phonetic.isNullOrBlank()) append(translation.phonetic)
            if (!translation.partOfSpeech.isNullOrBlank()) {
                if (isNotEmpty()) append(" • ")
                append(translation.partOfSpeech)
            }
        }
        cardBinding.tvPhonetic.isVisible = phoneticInfo.isNotEmpty()
        cardBinding.tvPhonetic.text = phoneticInfo

        // Examples
        val hasExample = !translation.exampleTarget.isNullOrBlank()
        cardBinding.layoutExample.isVisible = hasExample
        if (hasExample) {
            cardBinding.tvExampleTarget.text = translation.exampleTarget
            cardBinding.tvExampleEnglish.text = translation.exampleEnglish
        }

        // Bookmark Icon
        val bookmarkRes = if (translation.isBookmarked) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border
        cardBinding.btnBookmark.setIconResource(bookmarkRes)

        // Actions
        cardBinding.btnSpeak.setOnClickListener {
            ttsHelper.speak(translation.translatedText, translation.language)
        }

        cardBinding.btnCopy.setOnClickListener {
            copyToClipboard(translation.translatedText)
        }

        cardBinding.btnShare.setOnClickListener {
            shareTranslation(query, translation)
        }

        cardBinding.btnBookmark.setOnClickListener {
            viewModel.toggleFavorite(query, translation.isBookmarked)
        }
    }

    private fun applyFilterVisibility(filter: SelectedLanguageFilter) {
        when (filter) {
            SelectedLanguageFilter.ALL -> {
                binding.cardGerman.root.isVisible = true
                binding.cardNepali.root.isVisible = true
                binding.cardHindi.root.isVisible = true
            }
            SelectedLanguageFilter.GERMAN -> {
                binding.cardGerman.root.isVisible = true
                binding.cardNepali.root.isVisible = false
                binding.cardHindi.root.isVisible = false
            }
            SelectedLanguageFilter.NEPALI -> {
                binding.cardGerman.root.isVisible = false
                binding.cardNepali.root.isVisible = true
                binding.cardHindi.root.isVisible = false
            }
            SelectedLanguageFilter.HINDI -> {
                binding.cardGerman.root.isVisible = false
                binding.cardNepali.root.isVisible = false
                binding.cardHindi.root.isVisible = true
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Translation", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, getString(R.string.copied_toast), Toast.LENGTH_SHORT).show()
    }

    private fun shareTranslation(query: String, translation: SingleTranslation) {
        val shareText = "English: $query\n${translation.language.displayName}: ${translation.translatedText}\n\nTranslated with Any-Translate"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Share Translation"))
    }

    private fun showHistoryBottomSheet(isFavoritesOnly: Boolean) {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetHistoryBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.tvSheetTitle.text = if (isFavoritesOnly) {
            getString(R.string.favorites_title)
        } else {
            getString(R.string.history_title)
        }

        val adapter = HistoryAdapter(
            onItemClick = { clickedQuery ->
                binding.etSearchQuery.setText(clickedQuery)
                performTranslation()
                dialog.dismiss()
            },
            onToggleFavorite = { item ->
                viewModel.toggleFavorite(item.queryText, item.isFavorite)
            },
            onDeleteClick = { id ->
                viewModel.deleteHistory(id)
            }
        )

        sheetBinding.rvHistoryList.layoutManager = LinearLayoutManager(this)
        sheetBinding.rvHistoryList.adapter = adapter

        sheetBinding.btnClearAll.setOnClickListener {
            viewModel.clearHistory()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                val flow = if (isFavoritesOnly) viewModel.favoritesList else viewModel.historyList
                flow.collectLatest { list ->
                    adapter.submitList(list)
                    sheetBinding.tvEmptyHistory.isVisible = list.isEmpty()
                    sheetBinding.rvHistoryList.isVisible = list.isNotEmpty()
                }
            }
        }

        dialog.show()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.etSearchQuery.windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper.shutdown()
        binding.adView.destroy()
    }
}
