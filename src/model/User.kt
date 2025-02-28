package model

data class User (
    val id: Int,
    var name: String,
    val email: String,
    val password: String,
    val role: Int,
    var profileImage:String
) {
    companion object {
        var currentId: Int = 0 // Hält die aktuelle ID, die für den nächsten Benutzer verwendet wird.

        /**
         * Generiert eine neue, eindeutige ID für den Benutzer.
         *
         * @return Die nächste ID als Ganzzahl.
         */
        fun generateId(): Int {
            currentId++ // Erhöht die aktuelle ID um 1.
            return currentId
        }



    }


}