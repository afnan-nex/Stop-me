package com.afnan.stopme.feature.challenge

/**
 * Curated collection of psychologically uncomfortable paragraphs (40–50 words each).
 * Presented randomly during the unlock challenge to create deliberate friction.
 */
object ChallengeTextProvider {

    private val paragraphs = listOf(
        // 48 words
        "Every minute I spend scrolling is a minute I will never reclaim. The content disappearing from my screen in seconds demanded my full attention, yet I will recall none of it tomorrow. I am trading the finite hours of my life for a habit that gives nothing back.",

        // 48 words
        "The app I am trying to open has already consumed thirty minutes of my day. I decided to set this limit because I knew that without it I would spend far more. That version of myself was right. This moment of friction exists because I asked for it.",

        // 43 words
        "I am not missing anything important. Everyone I care about can reach me through a phone call. The notifications, the posts, the stories, and the updates will all exist later. They have no urgency. I am the one inventing urgency where none exists.",

        // 47 words
        "My attention is the most valuable resource I possess. Every company behind the app I am attempting to open has invested billions specifically to capture it and to prevent me from leaving. I am about to hand it over voluntarily, again, despite already having done so today.",

        // 49 words
        "Thirty minutes have already passed. I sat with a screen in my hand and let thirty minutes of my day disappear. I am now asking for thirty more. I should consider what I would rather have done with the time that is already gone before I give away more.",

        // 46 words
        "The version of me that set up this app understood something important. That version was calm, clear-headed, and honest about how I use my time. This version of me, the one trying to bypass the limit, is the version that past me was trying to protect me from.",

        // 44 words
        "There is nothing on the other side of this screen that will make me feel better about how I spent today. I have already checked it. Nothing changed in the last few minutes. Nothing will change in the next thirty. The feeling I am chasing does not exist there.",

        // 45 words
        "I set this limit because I care about my time. Not because I was told to, not because someone forced me, but because I made a deliberate choice. That choice still stands. The discomfort I feel right now is not a reason to abandon it. It is the reason it exists."
    )

    fun getRandom(): String = paragraphs.random()

    fun getAll(): List<String> = paragraphs
}
