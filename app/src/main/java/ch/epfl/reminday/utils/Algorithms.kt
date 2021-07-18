package ch.epfl.reminday.utils

object Algorithms {

    fun longestCommonSubsequence(s1: CharSequence, s2: CharSequence): String {
        val score = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) {
            for (j in 0..s2.length) {
                if (i == 0 || j == 0)
                    score[i][j] = 0
                else if (s1[i - 1] == s2[j - 1])
                    score[i][j] = score[i - 1][j - 1] + 1
                else
                    score[i][j] = maxOf(score[i - 1][j], score[i][j - 1])
            }
        }

        val lcs = CharArray(score[s1.length][s2.length])
        var i = s1.length - 1
        var j = s2.length - 1
        var idx = score[s1.length][s2.length] - 1
        while (i >= 0 && j >= 0) {
            when {
                s1[i] == s2[j] -> {
                    lcs[idx] = s1[i]
                    i -= 1
                    j -= 1
                    idx -= 1
                }
                score[i][j + 1] > score[i + 1][j] ->
                    i -= 1
                else ->
                    j -= 1
            }
        }

        return lcs.concatToString()
    }
}