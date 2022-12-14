package ru.netology.coroutines

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import ru.netology.coroutines.dto.Author
import ru.netology.coroutines.dto.Comment
import ru.netology.coroutines.dto.Post
import ru.netology.coroutines.dto.PostWithComments
import java.io.IOException
import java.sql.DriverManager
import java.sql.DriverManager.println
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/*
fun main() {
    runBlocking {
        println(Thread.currentThread().name)
    }
}
*/

/*
fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        println(Thread.currentThread().name)
    }

    Thread.sleep(1000L)
}
*/

/*
fun main() {
    val custom = Executors.newFixedThreadPool(64).asCoroutineDispatcher()
    with(CoroutineScope(EmptyCoroutineContext)) {
        launch(Dispatchers.Default) {
            println(Thread.currentThread().name)
        }
        launch(Dispatchers.IO) {
            println(Thread.currentThread().name)
        }
        // will throw exception without UI
        // launch(Dispatchers.Main) {
        //    println(Thread.currentThread().name)
        // }

        launch(custom) {
            println(Thread.currentThread().name)
        }
    }
    Thread.sleep(1000L)
    custom.close()
}
*/

/*
private val gson = Gson()
private val BASE_URL = "http://127.0.0.1:9999"
private val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor(::println).apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .connectTimeout(30, TimeUnit.SECONDS)
    .build()

fun main() {
    with(CoroutineScope(EmptyCoroutineContext)) {
        launch {
            try {
                val posts = getPosts(client)
                    .map { post ->
                        PostWithComments(post, getComments(client, post.id))
                    }
                println(posts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    Thread.sleep(30_000L)
}

suspend fun OkHttpClient.apiCall(url: String): Response {
    return suspendCoroutine { continuation ->
        Request.Builder()
            .url(url)
            .build()
            .let(::newCall)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
    }
}

suspend fun <T> makeRequest(url: String, client: OkHttpClient, typeToken: TypeToken<T>): T =
    withContext(Dispatchers.IO) {
        client.apiCall(url)
            .let { response ->
                if (!response.isSuccessful) {
                    response.close()
                    throw RuntimeException(response.message)
                }
                val body = response.body ?: throw RuntimeException("response body is null")
                gson.fromJson(body.string(), typeToken.type)
            }
    }

suspend fun getPosts(client: OkHttpClient): List<Post> =
    makeRequest("$BASE_URL/api/slow/posts", client, object : TypeToken<List<Post>>() {})

suspend fun getComments(client: OkHttpClient, id: Long): List<Comment> =
    makeRequest("$BASE_URL/api/slow/posts/$id/comments", client, object : TypeToken<List<Comment>>() {})
*/

/*

private val gson = Gson()
private val BASE_URL = "http://127.0.0.1:9999"
private val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor(::println).apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .connectTimeout(30, TimeUnit.SECONDS)
    .build()

fun main() {
    with(CoroutineScope(EmptyCoroutineContext)) {
        launch {
            try {
                val posts = getPosts(client)
                    .map { post ->
                        async {
                            PostWithComments(post, getComments(client, post.id))
                        }
                    }.awaitAll()
                println(posts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    Thread.sleep(30_000L)
}

suspend fun OkHttpClient.apiCall(url: String): Response {
    return suspendCoroutine { continuation ->
        Request.Builder()
            .url(url)
            .build()
            .let(::newCall)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
    }
}

suspend fun <T> makeRequest(url: String, client: OkHttpClient, typeToken: TypeToken<T>): T =
    withContext(Dispatchers.IO) {
        client.apiCall(url)
            .let { response ->
                if (!response.isSuccessful) {
                    response.close()
                    throw RuntimeException(response.message)
                }
                val body = response.body ?: throw RuntimeException("response body is null")
                gson.fromJson(body.string(), typeToken.type)
            }
    }

suspend fun getPosts(client: OkHttpClient): List<Post> =
    makeRequest("$BASE_URL/api/slow/posts", client, object : TypeToken<List<Post>>() {})

suspend fun getComments(client: OkHttpClient, id: Long): List<Comment> =
    makeRequest("$BASE_URL/api/slow/posts/$id/comments", client, object : TypeToken<List<Comment>>() {})*/

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(HttpLoggingInterceptor().apply{
        level = HttpLoggingInterceptor.Level.BODY
    }).build()

private val gson = Gson()
private const val BASE_URL = "http://127.0.0.1:9999"


/*suspend fun <T> makeCall(url: String, typeToken: TypeToken<T>): T =
    suspendCoroutine { continuation ->
        Request.Builder()
            .url(url)
            .build()
            .let(client::newCall)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        continuation.resume(gson.fromJson(response.body?.string(), typeToken.type))
                    } catch (e: JsonParseException) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }
            })
    }*/

suspend fun <T> makeCall(url: String, typeToken: TypeToken<T>): T =
    withContext(Dispatchers.IO) {
        Request.Builder()
            .url(url)
            .build()
            .let(client::newCall)
            .execute()
            .let { response ->
                gson.fromJson(response.body?.string(), typeToken.type)
            }
    }


suspend fun getPosts(): List<Post> =
    makeCall("$BASE_URL/api/slow/posts", object : TypeToken<List<Post>>() {})

suspend fun getComments(postId: Long): List<Comment> =
    makeCall("$BASE_URL/api/slow/posts/$postId/comments/", object : TypeToken<List<Comment>>() {})

suspend fun getAuthor(id: Long): Author =
    makeCall("$BASE_URL/api/slow/authors/$id", object : TypeToken<Author>() {})

fun main() {
    //runBlocking+ ?????????????????????? ??????????????????????????????
//    runBlocking {
//        val posts = getPosts()
//
//        val result = posts.map{
//            PostWithComments(it, getComments(it.id))
//        }
//
//        println(result)
//    }
    //runBlocking-

    with(CoroutineScope(EmptyCoroutineContext)){
        launch {
            try {
                val posts = getPosts()
                    .map {post ->
                        async {
                            val author = async { getAuthor(post.authorId) }
                            val comments = async { getComments(post.id) }
                            PostWithComments(post, comments.await(), author.await())
                        }
                    }.awaitAll()
                //println(posts)
                posts.map{post ->

                    println(post.author)
                    println(post.post)
                    println(post.comments)

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Thread.sleep(30_000L)
}