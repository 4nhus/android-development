package org.hyperskill.ordersmenu

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hyperskill.ordersmenu.theme.PlayOrdersMenuTheme

class MainActivity : ComponentActivity() {
    private val menuItems = mapOf(
        "Fettuccine" to Pair(mutableStateOf(0), mutableStateOf(5)),
        "Risotto" to Pair(mutableStateOf(0), mutableStateOf(6)),
        "Gnocchi" to Pair(mutableStateOf(0), mutableStateOf(4)),
        "Spaghetti" to Pair(mutableStateOf(0), mutableStateOf(3)),
        "Lasagna" to Pair(mutableStateOf(0), mutableStateOf(5)),
        "Steak Parmigiana" to Pair(mutableStateOf(0), mutableStateOf(2))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Menu(menuItems = menuItems)
        }
    }

    @Composable
    fun MenuItem(
        name: String,
        orderAmount: MutableState<Int>,
        stockAmount: MutableState<Int>,
        fontSize: TextUnit
    ) {
        var amountOrdered by rememberSaveable { orderAmount }
        val amountStock by rememberSaveable { stockAmount }
        val orderedMax by remember { derivedStateOf { amountOrdered == amountStock } }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = name,
                fontSize = fontSize,
                color = if (orderedMax) Color.Red else Color.Black
            )
            Text(text = "+", fontSize = fontSize, modifier = Modifier.clickable {
                if (amountOrdered < amountStock) amountOrdered++
            })
            Text(text = amountOrdered.toString(), fontSize = fontSize)
            Text(text = "-", fontSize = fontSize, modifier = Modifier.clickable {
                if (amountOrdered > 0) amountOrdered--
            })
        }
    }

    @Composable
    fun Menu(menuItems: Map<String, Pair<MutableState<Int>, MutableState<Int>>>) {
        PlayOrdersMenuTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Orders Menu", fontSize = 48.sp)
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        menuItems.forEach { menuItem ->
                            MenuItem(
                                name = menuItem.key,
                                orderAmount = menuItem.value.first,
                                stockAmount = menuItem.value.second,
                                fontSize = 24.sp
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                val orderMessage = StringBuilder("Ordered:")
                                menuItems.forEach { menuItem ->
                                    val orderAmount = menuItem.value.first.value
                                    if (orderAmount > 0) {
                                        menuItem.value.second.value -= orderAmount

                                        orderMessage.append("\n==> ${menuItem.key}: $orderAmount")

                                        menuItem.value.first.value = 0
                                    }
                                }
                                if (!orderMessage.contentEquals("Ordered:")) Toast.makeText(
                                    this@MainActivity,
                                    orderMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Make Order", fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("UnrememberedMutableState")
    @Preview
    @Composable
    fun MainActivityPreview() {
        Menu(
            mapOf(
                "Fettuccine" to Pair(mutableStateOf(0), mutableStateOf(5)),
                "Risotto" to Pair(mutableStateOf(0), mutableStateOf(6)),
                "Gnocchi" to Pair(mutableStateOf(0), mutableStateOf(4)),
                "Spaghetti" to Pair(mutableStateOf(0), mutableStateOf(3)),
                "Lasagna" to Pair(mutableStateOf(0), mutableStateOf(5)),
                "Steak Parmigiana" to Pair(mutableStateOf(0), mutableStateOf(2))
            )
        )
    }
}


