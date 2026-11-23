package pe.moviwallet.passenger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import pe.moviwallet.passenger.ui.navigation.MoviWalletApp
import pe.moviwallet.passenger.ui.theme.MoviWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoviWalletTheme {
                MoviWalletApp()
            }
        }
    }
}
