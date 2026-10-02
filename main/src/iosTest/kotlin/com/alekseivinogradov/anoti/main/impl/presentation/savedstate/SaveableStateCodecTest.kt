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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.snapshots.SnapshotMutableState
import androidx.compose.runtime.structuralEqualityPolicy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

// One function per case under test, plus the helpers those cases share.
@Suppress("TooManyFunctions")
class SaveableStateCodecTest {

    @Test
    fun everyPlainTypeComesBackAsTheSameType() {
        //Given
        val values = listOf<Any?>(null, "text", true, 7, 7L, 1.5f, 1.5)

        //When
        val restored = roundTrip(values)

        //Then
        assertEquals(values, restored)
        assertEquals(
            listOf(null, "String", "Boolean", "Int", "Long", "Float", "Double"),
            restored.map { it?.let { value: Any -> value::class.simpleName } }
        )
    }

    @Test
    fun floatsAndDoublesComeBackExactly() {
        //Given
        val floats =
            listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, -0.0f, 0.1f)
        val doubles =
            listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -0.0, 0.1)

        //When
        val restoredFloats = roundTrip(floats)
        val restoredDoubles = roundTrip(doubles)

        //Then
        assertEquals(
            floats.map { it.toRawBits() },
            restoredFloats.map { (it as Float).toRawBits() }
        )
        assertEquals(
            doubles.map { it.toRawBits() },
            restoredDoubles.map { (it as Double).toRawBits() }
        )
    }

    @Test
    fun boundaryValuesComeBackExactly() {
        //Given
        val values = listOf<Any?>(
            Long.MAX_VALUE,
            Long.MIN_VALUE,
            Int.MIN_VALUE,
            "",
            "Фрирен 葬送のフリーレン \uD83C\uDF38",
            emptyList<Any?>(),
            emptyMap<Any?, Any?>()
        )

        //When
        val restored = roundTrip(values)

        //Then
        assertEquals(values, restored)
    }

    @Test
    fun nestedListsAndMapsComeBackWithTheirKeys() {
        //Given
        val values = listOf<Any?>(
            listOf(1, listOf("a", null)),
            mapOf(1 to "one", "two" to listOf(2L), 3L to mapOf(true to null))
        )

        //When
        val restored = roundTrip(values)

        //Then
        assertEquals(values, restored)
    }

    @Test
    fun theScreensStateAScreenStackSavesComesBackUnderEachScreensKey() {
        //Given
        // A stack of screens saves one map: each screen's key to what that screen saved.
        val screens = mapOf<Any, Map<String, List<Any?>>>(
            "AnimeList" to mapOf("search" to listOf(mutableStateOf("frieren"))),
            "AnimeFavorites" to mapOf("scroll" to listOf(3, 120))
        )

        //When
        val restored = roundTrip(listOf(screens)).single()

        //Then
        assertIs<Map<*, *>>(restored)
        val list = assertIs<Map<*, *>>(restored["AnimeList"])
        val search = assertIs<SnapshotMutableState<*>>(assertIs<List<*>>(list["search"]).single())
        assertEquals("frieren", search.value)
        assertEquals(mapOf("scroll" to listOf(3, 120)), restored["AnimeFavorites"])
    }

    @Test
    fun aLazyListsIndexAndOffsetComeBackAsInts() {
        //Given
        val values = listOf<Any?>(listOf(12, 340))

        //When
        val restored = roundTrip(values)

        //Then
        assertEquals(values, restored)
        assertIs<Int>((restored.single() as List<*>)[1])
    }

    @Test
    fun aStateComesBackWithItsValueAndItsPolicy() {
        //Given
        val policies = listOf(
            structuralEqualityPolicy(),
            referentialEqualityPolicy(),
            neverEqualPolicy<Any?>()
        )
        val states = policies.map { mutableStateOf("text", it) }

        //When
        val restored = roundTrip(states)

        //Then
        restored.zip(policies).forEach { (state: Any?, policy) ->
            assertIs<SnapshotMutableState<*>>(state)
            assertEquals("text", state.value)
            assertSame(policy, state.policy)
        }
    }

    @Test
    fun eachSpecializedStateComesBackAsTheSameKind() {
        //Given
        val states = listOf(
            mutableIntStateOf(3),
            mutableLongStateOf(4L),
            mutableFloatStateOf(-0.0f),
            mutableDoubleStateOf(Double.NaN)
        )

        //When
        val restored = roundTrip(states)

        //Then
        assertEquals(3, assertIs<MutableIntState>(restored[0]).intValue)
        assertEquals(4L, assertIs<MutableLongState>(restored[1]).longValue)
        assertEquals(
            (-0.0f).toRawBits(),
            assertIs<MutableFloatState>(restored[2]).floatValue.toRawBits()
        )
        assertTrue(assertIs<MutableDoubleState>(restored[3]).doubleValue.isNaN())
    }

    @Test
    fun restoredListsAndMapsCanBeChanged() {
        //Given
        val values = listOf<Any?>(listOf(1), mapOf("key" to 1))

        //When
        val restored = roundTrip(values)

        //Then
        val list = assertIs<MutableList<Any?>>(restored[0])
        val map = assertIs<MutableMap<Any?, Any?>>(restored[1])
        list += 2
        map["other"] = 2
        assertEquals(listOf<Any?>(1, 2), list)
        assertEquals(mapOf<Any?, Any?>("key" to 1, "other" to 2), map)
    }

    @Test
    fun aValueOfAnUnknownTypeDropsOnlyItsOwnKey() {
        //Given
        val values = mapOf(
            "kept" to listOf<Any?>(1, 2),
            "dropped" to listOf<Any?>(1, Unknown),
            "stateWithItsOwnPolicy" to listOf<Any?>(mutableStateOf(1, OwnPolicyFake)),
            "stateList" to listOf<Any?>(mutableStateListOf(1)),
            "stateMap" to listOf<Any?>(mutableStateMapOf(1 to 1))
        )

        //When
        val restored = SaveableStateCodec.decode(SaveableStateCodec.encode(values))

        //Then
        assertEquals(mapOf("kept" to listOf<Any?>(1, 2)), restored)
    }

    @Test
    fun jsonOfAnotherShapeRestoresNothingAndDoesNotThrow() {
        //Given
        val unknownType =
            JsonObject(mapOf("t" to JsonPrimitive("unknown"), "v" to JsonPrimitive(1)))
        val shapes = listOf(
            JsonPrimitive("text"),
            JsonArray(listOf(JsonPrimitive(1))),
            buildJsonObject { put("key", "not a list") },
            buildJsonObject {
                put("key", JsonArray(listOf(unknownType)))
            }
        )

        //When
        val restored = shapes.map(SaveableStateCodec::decode)

        //Then
        assertEquals(List(shapes.size) { emptyMap() }, restored)
    }

    // Through the text the file holds, where a number JSON cannot spell would fail.
    private fun roundTrip(values: List<Any?>): List<Any?> {
        val text = Json.encodeToString(
            JsonElement.serializer(),
            SaveableStateCodec.encode(mapOf("key" to values))
        )
        return SaveableStateCodec.decode(Json.parseToJsonElement(text)).getValue("key")
    }

    private object Unknown

    private object OwnPolicyFake : SnapshotMutationPolicy<Int> {
        override fun equivalent(a: Int, b: Int): Boolean = a == b
    }
}
