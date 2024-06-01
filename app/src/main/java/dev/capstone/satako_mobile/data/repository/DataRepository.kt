package dev.capstone.satako_mobile.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import dev.capstone.satako_mobile.data.response.Result
import dev.capstone.satako_mobile.data.pref.UserPreference
import dev.capstone.satako_mobile.data.response.ErrorResponse
import dev.capstone.satako_mobile.data.response.HistoryResponse
import dev.capstone.satako_mobile.data.response.LoginResponse
import dev.capstone.satako_mobile.data.response.PredictResponse
import dev.capstone.satako_mobile.data.response.RegisterResponse
import dev.capstone.satako_mobile.data.retrofit.ApiConfig
import dev.capstone.satako_mobile.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import retrofit2.HttpException

class DataRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    suspend fun saveSession(token: String) {
        userPreference.saveSession(token)
    }

    fun getSession(): Flow<String> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun register(
        name: String,
        email: String,
        password: String
    ): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun login(
        email: String,
        password: String
    ): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun predict(
        file: MultipartBody.Part
    ): LiveData<Result<PredictResponse>> = liveData {
        emit(Result.Loading)
        try {
            val token = runBlocking { userPreference.getSession().first() }
            val apiService = ApiConfig.getApiService(token)
            val response = apiService.predict(file)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            errorMessage?.let { Result.Error(it) }?.let { emit(it) }
        }
    }

    fun getHistory(): LiveData<Result<HistoryResponse>> = liveData {
        emit(Result.Loading)
        try {
            val token = runBlocking { userPreference.getSession().first() }
            val apiService = ApiConfig.getApiService(token)
            val response = apiService.getHistory()
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            errorMessage?.let { Result.Error(it) }?.let { emit(it) }
        }

    }

    companion object {
        @Volatile
        private var instance: DataRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): DataRepository =
            instance ?: synchronized(this) {
                instance ?: DataRepository(userPreference, apiService)
            }.also { instance = it }
    }

}