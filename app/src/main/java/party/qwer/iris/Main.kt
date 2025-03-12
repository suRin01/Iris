// SendMsg : ye-seola/go-kdb
// Kakaodecrypt : jiru/kakaodecrypt
package party.qwer.iris

import java.io.File
import java.util.concurrent.TimeUnit

const val IMAGE_DIR_PATH: String = "/sdcard/Android/data/com.kakao.talk/files"

class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val notificationReferer = readNotificationReferer()

                Replier.startMessageSender()
                println("Message sender thread started")

                val kakaoDb = KakaoDB()
                val observerHelper = ObserverHelper(kakaoDb)

                val dbObserver = DBObserver(kakaoDb, observerHelper)
                dbObserver.startPolling()
                println("DBObserver started")

                val imageDeleter = ImageDeleter(IMAGE_DIR_PATH, TimeUnit.HOURS.toMillis(1))
                imageDeleter.startDeletion()
                println("ImageDeleter started, and will delete images older than 1 hour.")

                val irisServer =
                    IrisServer(kakaoDb, dbObserver, observerHelper, notificationReferer)
                irisServer.startServer()
                println("Iris Server started")

                kakaoDb.closeConnection()
            } catch (e: Exception) {
                System.err.println("Iris Error")
                e.printStackTrace()
            }
        }

        private fun readNotificationReferer(): String {
            val prefsFile =
                File("/data/data/com.kakao.talk/shared_prefs/KakaoTalk.hw.perferences.xml")
            val data = prefsFile.bufferedReader().use {
                it.readText()
            }
            val regex = Regex("""<string name="NotificationReferer">(.*?)</string>""")
            val match = regex.find(data) ?: throw Exception("failed to extract referer from data")

            return match.value
        }
    }
}

