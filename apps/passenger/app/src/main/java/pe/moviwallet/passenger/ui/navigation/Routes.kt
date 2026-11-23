package pe.moviwallet.passenger.ui.navigation

object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECOVER = "recover"

    const val HOME = "home"
    const val WALLET = "wallet"
    const val OPERATORS = "operators"
    const val PROFILE = "profile"

    const val TOPUP = "topup"
    const val PAY = "pay"
    const val TRANSFER = "transfer"
    const val TX_DETAIL = "transaction/{id}"
    const val RECEIPT = "receipt/{id}"

    fun txDetail(id: String) = "transaction/$id"
    fun receipt(id: String) = "receipt/$id"

    val TABS = setOf(HOME, WALLET, OPERATORS, PROFILE)
}
