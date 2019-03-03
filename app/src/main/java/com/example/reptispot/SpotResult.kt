package com.example.reptispot

import com.microsoft.projectoxford.face.contract.Face

data class SpotResult(
    val face: Face,
    val matchRate: Float
)