package com.example.reptispot

import android.support.annotation.WorkerThread
import com.google.gson.Gson
import com.microsoft.projectoxford.face.FaceServiceClient
import com.microsoft.projectoxford.face.contract.TrainingStatus
import com.microsoft.projectoxford.face.rest.ClientException
import java.io.InputStream
import kotlin.random.Random

class ReptiSpot(
    private val client: FaceServiceClient,
    private val confidenceThreshold: Float = 0.5F,
    private val largePersonGroupId: String,
    private val imageStream: InputStream //todo: maybe pass an image, not a stream
) {

    private var trainingStatus: TrainingStatus? = null

    //todo: exception handle

    @WorkerThread
    fun process(): List<SpotResult> {

        val faces = client.detect(
            imageStream,
            true,
            false,
            emptyArray()
        )
        val facesWithCandidates = faces.map { FaceWithCandidate(it) }.associateBy { it.face.faceId }

        try {
            trainingStatus = client.getLargePersonGroupTrainingStatus(largePersonGroupId)
        } catch (e: ClientException) {
            //group have never been trained or request failed, do nothing
        }

        if (trainingStatus?.status == TrainingStatus.Status.Succeeded) {
            //try to identify faces on photo
            //todo: 10 face per request limit
            val identifyResults = client.identityInLargePersonGroup(
                largePersonGroupId,
                faces.map { it.faceId }.toTypedArray(),
                confidenceThreshold,
                1
            )
            identifyResults.forEach {
                if (it.candidates.size > 0) {
                    facesWithCandidates[it.faceId]?.personCandidate = it.candidates[0]
                }
            }
        }

        val spotResults = mutableListOf<SpotResult>()
        facesWithCandidates.forEach {
            val matchRate = handleFaceWithCandidate(it.value)
            spotResults.add(SpotResult(it.value.face, matchRate))
        }

        return spotResults
    }

    private fun handleFaceWithCandidate(faceWithCandidate: FaceWithCandidate): Float {
        val personId = faceWithCandidate.personCandidate?.personId

        if (personId != null) {
            val person = client.getPersonInLargePersonGroup(largePersonGroupId, personId)

            //add current face to existing person, if confidence is not 1
            if (faceWithCandidate.personCandidate?.confidence?.compareTo(1) == 0) {
                imageStream.reset()
                client.addPersonFaceInLargePersonGroup(
                    largePersonGroupId,
                    personId,
                    imageStream,
                    "",
                    faceWithCandidate.face.faceRectangle
                )
                train()
            }

            return Gson().fromJson(person.userData, UserData::class.java).matchRate
        }

        val matchRate = Random.nextFloat()

        //create person with current face and generated rate
        val userData = UserData(matchRate)
        val userDataString = Gson().toJson(userData)
        val createPersonResult = client.createPersonInLargePersonGroup(largePersonGroupId, "unknown", userDataString)
        imageStream.reset()
        client.addPersonFaceInLargePersonGroup(
            largePersonGroupId,
            createPersonResult.personId,
            imageStream,
            "",
            faceWithCandidate.face.faceRectangle
        )
        train()

        return matchRate
    }

    private fun train() {
        //todo: delay by last train date, fetched from status
        client.trainLargePersonGroup(largePersonGroupId)
    }
}