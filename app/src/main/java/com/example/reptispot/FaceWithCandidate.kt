package com.example.reptispot

import com.microsoft.projectoxford.face.contract.Candidate
import com.microsoft.projectoxford.face.contract.Face

data class FaceWithCandidate(
    val face: Face,
    var personCandidate: Candidate? = null
)