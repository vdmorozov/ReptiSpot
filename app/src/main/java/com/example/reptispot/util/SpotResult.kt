package com.example.reptispot.util

import com.microsoft.projectoxford.face.contract.Face

data class SpotResult(
    val face: Face,
    val matchRate: Float
)