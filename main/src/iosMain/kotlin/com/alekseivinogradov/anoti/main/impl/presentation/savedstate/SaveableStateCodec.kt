package com.alekseivinogradov.anoti.main.impl.presentation.savedstate

import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.SnapshotMutationPolicy
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.structuralEqualityPolicy
import com.alekseivinogradov.anoti.celebrity.kmp.api.domain.ANOTI_TAG
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

private const val TAG = "SaveableStateCodec"

/**
 * Turns what a Compose saveable-state registry saves into JSON and back, for a platform with no
 * saved-instance state of its own to keep it in.
 *
 * Every value carries a tag naming its type, so each comes back as the same type. A value of a
 * type it does not know drops its whole key, since a saver restoring part of its list would
 * fail. Nothing here throws.
 */
internal object SaveableStateCodec {

    private const val TYPE = "t"
    private const val VALUE = "v"
    private const val POLICY = "p"

    private const val STRING = "string"
    private const val BOOLEAN = "boolean"
    private const val INT = "int"
    private const val LONG = "long"
    private const val FLOAT = "float"
    private const val DOUBLE = "double"
    private const val LIST = "list"
    private const val MAP = "map"
    private const val STATE = "state"
    private const val INT_STATE = "intState"
    private const val LONG_STATE = "longState"
    private const val FLOAT_STATE = "floatState"
    private const val DOUBLE_STATE = "doubleState"

    /** Writes [values], each key's list of saved values, as JSON. */
    fun encode(values: Map<String, List<Any?>>): JsonElement =
        JsonObject(
            values.mapNotNull { (key: String, saved: List<Any?>) ->
                runCatching { key to JsonArray(saved.map(::encodeValue)) }
                    .onFailure { println("$ANOTI_TAG $TAG: $key was not saved: $it") }
                    .getOrNull()
            }.toMap()
        )

    /** Reads back what [encode] wrote. A key that does not read back is left out. */
    fun decode(element: JsonElement): Map<String, List<Any?>> {
        val keys = element as? JsonObject ?: run {
            println("$ANOTI_TAG $TAG: nothing was restored, the saved state is not an object")
            return emptyMap()
        }
        return keys.mapNotNull { (key: String, saved: JsonElement) ->
            runCatching { key to saved.jsonArray.mapTo(mutableListOf(), ::decodeValue) }
                .onFailure { println("$ANOTI_TAG $TAG: $key was not restored: $it") }
                .getOrNull()
        }.toMap()
    }

    private fun encodeValue(value: Any?): JsonElement = when (value) {
        null -> JsonNull
        // They would come back as a plain list or map and fail their cast on restore.
        is SnapshotStateList<*>, is SnapshotStateMap<*, *> ->
            throw IllegalArgumentException("a ${value::class.simpleName} cannot be saved")

        is List<*> -> tagged(LIST, JsonArray(value.map(::encodeValue)))
        is Map<*, *> -> tagged(
            MAP,
            JsonArray(
                value.map { (key: Any?, item: Any?) ->
                    JsonArray(listOf(encodeValue(key), encodeValue(item)))
                }
            )
        )

        else -> encodePrimitive(value)
    }

    // The raw bits bring every Float and Double back exactly, NaN and negative zero included.
    private fun encodePrimitive(value: Any): JsonElement = when (value) {
        is String -> tagged(STRING, JsonPrimitive(value))
        is Boolean -> tagged(BOOLEAN, JsonPrimitive(value))
        is Int -> tagged(INT, JsonPrimitive(value))
        is Long -> tagged(LONG, JsonPrimitive(value))
        is Float -> tagged(FLOAT, JsonPrimitive(value.toRawBits()))
        is Double -> tagged(DOUBLE, JsonPrimitive(value.toRawBits()))
        else -> encodeState(value)
    }

    // The specialized states come before the general one, since each of them is one too.
    private fun encodeState(value: Any): JsonElement = when (value) {
        is MutableIntState -> tagged(INT_STATE, JsonPrimitive(value.intValue))
        is MutableLongState -> tagged(LONG_STATE, JsonPrimitive(value.longValue))
        is MutableFloatState -> tagged(FLOAT_STATE, JsonPrimitive(value.floatValue.toRawBits()))
        is MutableDoubleState ->
            tagged(DOUBLE_STATE, JsonPrimitive(value.doubleValue.toRawBits()))

        is SnapshotMutableState<*> -> tagged(
            STATE,
            encodeValue(value.value),
            policy = SavedPolicy.of(value.policy).name
        )

        else -> throw IllegalArgumentException("a ${value::class.simpleName} cannot be saved")
    }

    private fun decodeValue(element: JsonElement): Any? {
        if (element is JsonNull) return null
        val tagged = element.jsonObject
        val value = tagged.getValue(VALUE)
        return when (val type = tagged.getValue(TYPE).jsonPrimitive.content) {
            LIST -> value.jsonArray.mapTo(mutableListOf(), ::decodeValue)
            MAP -> value.jsonArray.associateTo(mutableMapOf()) { entry: JsonElement ->
                val (key: JsonElement, item: JsonElement) = entry.jsonArray
                decodeValue(key) to decodeValue(item)
            }

            STATE -> mutableStateOf(
                decodeValue(value),
                SavedPolicy.valueOf(tagged.getValue(POLICY).jsonPrimitive.content).policy
            )

            INT_STATE, LONG_STATE, FLOAT_STATE, DOUBLE_STATE ->
                decodeSpecializedState(type, value.jsonPrimitive)

            else -> decodePrimitive(type, value.jsonPrimitive)
        }
    }

    private fun decodePrimitive(type: String, value: JsonPrimitive): Any = when (type) {
        STRING -> value.also { require(it.isString) }.content
        BOOLEAN -> value.boolean
        INT -> value.int
        LONG -> value.long
        FLOAT -> Float.fromBits(value.int)
        DOUBLE -> Double.fromBits(value.long)
        else -> throw IllegalArgumentException("unknown type $type")
    }

    private fun decodeSpecializedState(type: String, value: JsonPrimitive): Any = when (type) {
        INT_STATE -> mutableIntStateOf(value.int)
        LONG_STATE -> mutableLongStateOf(value.long)
        FLOAT_STATE -> mutableFloatStateOf(Float.fromBits(value.int))
        else -> mutableDoubleStateOf(Double.fromBits(value.long))
    }

    private fun tagged(type: String, value: JsonElement, policy: String? = null) = JsonObject(
        buildMap {
            put(TYPE, JsonPrimitive(type))
            put(VALUE, value)
            policy?.let { put(POLICY, JsonPrimitive(it)) }
        }
    )
}

// The policies a saved state can carry, under the names it is saved with. A state with any other
// policy is not saved.
private enum class SavedPolicy(val policy: SnapshotMutationPolicy<Any?>) {
    STRUCTURAL(structuralEqualityPolicy()),
    REFERENTIAL(referentialEqualityPolicy()),
    NEVER_EQUAL(neverEqualPolicy());

    companion object {
        fun of(policy: SnapshotMutationPolicy<*>): SavedPolicy =
            entries.first { it.policy == policy }
    }
}
