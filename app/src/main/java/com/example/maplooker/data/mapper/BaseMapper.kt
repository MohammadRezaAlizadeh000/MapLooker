package com.example.maplooker.data.mapper

interface BaseMapper<T, R> {
    fun map(data: T): R
}