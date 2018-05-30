package com.sample

import kotlin.Boolean
import kotlin.String

interface JoinedWithUsing {
    val name: String

    val is_cool: Boolean

    data class Impl(override val name: String, override val is_cool: Boolean) : JoinedWithUsing
}
