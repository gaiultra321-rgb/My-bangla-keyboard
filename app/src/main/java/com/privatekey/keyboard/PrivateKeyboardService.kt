package com.privatekey.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.content.ClipboardManager
import android.content.Context

class PrivateKeyboardService : InputMethodService() {

    private enum class Mode { ENGLISH, BANGLA_PHONETIC, BANGLA_ABC }
    private var currentMode = Mode.ENGLISH

    override fun onCreateInputView(): View {
        return createKeyboardLayout()
    }

    private fun createKeyboardLayout(): View {
        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF222222.toInt()) // Dark Background
        }

        // Top Toolbar (Clipboard, Emoji & Layout Switch)
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val btnSwitch = Button(this).apply {
            text = when(currentMode) {
                Mode.ENGLISH -> "EN"
                Mode.BANGLA_PHONETIC -> "বাংলা (Phonetic)"
                Mode.BANGLA_ABC -> "বাংলা (ABC)"
            }
            setOnClickListener {
                currentMode = when(currentMode) {
                    Mode.ENGLISH -> Mode.BANGLA_PHONETIC
                    Mode.BANGLA_PHONETIC -> Mode.BANGLA_ABC
                    Mode.BANGLA_ABC -> Mode.ENGLISH
                }
                setInputView(createKeyboardLayout()) // Refresh view
            }
        }

        val btnClipboard = Button(this).apply {
            text = "📋 Paste"
            setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text
                    currentInputConnection?.commitText(text, 1)
                }
            }
        }

        val btnEmoji = Button(this).apply {
            text = "😊"
            setOnClickListener {
                // System Emoji Panel or simple emoji list
                currentInputConnection?.commitText("😊", 1)
            }
        }

        toolbar.addView(btnSwitch)
        toolbar.addView(btnClipboard)
        toolbar.addView(btnEmoji)
        rootLayout.addView(toolbar)

        // Simple Key Grid Generator
        val keys = when(currentMode) {
            Mode.ENGLISH -> listOf("q","w","e","r","t","y","u","i","o","p","a","s","d","f","g","h","j","k","l","z","x","c","v","b","n","m")
            Mode.BANGLA_ABC -> listOf("অ","আ","ই","ঈ","উ","ঊ","ক","খ","গ","ঘ","ঙ","চ","ছ","জ","ঝ","ঞ","ট","ঠ","ড","ঢ","ণ","ত","থ","দ","ধ","ন")
            Mode.BANGLA_PHONETIC -> listOf("a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z")
        }

        var row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        var count = 0

        for (key in keys) {
            val btn = Button(this).apply {
                text = key
                setOnClickListener {
                    handleKeyInput(key)
                }
            }
            row.addView(btn)
            count++
            if (count % 7 == 0) {
                rootLayout.addView(row)
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            }
        }
        rootLayout.addView(row)

        // Space & Backspace Row
        val bottomRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val btnSpace = Button(this).apply {
            text = "Space"
            setOnClickListener { currentInputConnection?.commitText(" ", 1) }
        }
        val btnDelete = Button(this).apply {
            text = "⌫"
            setOnClickListener { currentInputConnection?.deleteSurroundingText(1, 0) }
        }
        bottomRow.addView(btnSpace)
        bottomRow.addView(btnDelete)
        rootLayout.addView(bottomRow)

        return rootLayout
    }

    private fun handleKeyInput(key: String) {
        val ic = currentInputConnection ?: return
        if (currentMode == Mode.BANGLA_PHONETIC) {
            // Simple Phonetic Map Example (Expandable for full dictionary)
            val converted = when(key) {
                "ami" -> "আমি"
                "a" -> "আ"
                "k" -> "ক"
                "g" -> "গ"
                else -> key
            }
            ic.commitText(converted, 1)
        } else {
            ic.commitText(key, 1)
        }
    }
}
