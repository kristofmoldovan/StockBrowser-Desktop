package com.stockbrowser

enum class State {
    API_KEYS, //Az az állapot, amikor az API kulcsokat állítom be
    USE,      //Az az állapot, amikor az API kulcsok helyesen be vannak állítva, használható az alkalmazás
    WAITING   //Az az állapot, amikor hálózati hívás válaszára vár az alkalmazás (minden le van tiltva addig)
}