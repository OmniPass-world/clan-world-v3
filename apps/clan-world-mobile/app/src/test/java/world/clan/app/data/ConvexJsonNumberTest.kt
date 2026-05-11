package world.clan.app.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvexJsonNumberTest {

  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
  }

  @Test
  fun normalizeWholeNumberFloatToLong() {
    val raw = """{"tick":1.0,"id":60.0}"""
    val element = json.parseToJsonElement(raw).normalizeConvexNumbers()
    assertEquals(1L, element.jsonObject["tick"]!!.jsonPrimitive.longOrNull)
    assertEquals(60L, element.jsonObject["id"]!!.jsonPrimitive.longOrNull)
  }

  @Test
  fun normalizePreservesFractionalDoubles() {
    val raw = """{"amount":1.5,"ratio":0.25}"""
    val element = json.parseToJsonElement(raw).normalizeConvexNumbers()
    // Fractional doubles MUST remain doubles — Long-truncation would silently
    // discard `0.5` and break `vault.amount` and similar fields.
    assertNull(element.jsonObject["amount"]!!.jsonPrimitive.longOrNull)
    assertEquals(1.5, element.jsonObject["amount"]!!.jsonPrimitive.content.toDouble(), 0.0)
    assertEquals(0.25, element.jsonObject["ratio"]!!.jsonPrimitive.content.toDouble(), 0.0)
  }

  @Test
  fun normalizePreservesStrings() {
    val raw = """{"goldBalance":"1000000000000000000","name":"clan-alpha"}"""
    val element = json.parseToJsonElement(raw).normalizeConvexNumbers()
    assertTrue(element.jsonObject["goldBalance"]!!.jsonPrimitive.isString)
    assertEquals("1000000000000000000", element.jsonObject["goldBalance"]!!.jsonPrimitive.content)
    assertEquals("clan-alpha", element.jsonObject["name"]!!.jsonPrimitive.content)
  }

  @Test
  fun normalizePreservesBareIntegers() {
    val raw = """{"tick":42,"region":3}"""
    val element = json.parseToJsonElement(raw).normalizeConvexNumbers()
    assertEquals(42L, element.jsonObject["tick"]!!.jsonPrimitive.longOrNull)
    assertEquals(3L, element.jsonObject["region"]!!.jsonPrimitive.longOrNull)
  }

  @Test
  fun normalizeNestedObjectsAndArrays() {
    val raw = """
      {
        "tick": 12.0,
        "bandit": {
          "id": 1.0,
          "attackPower": 60.0,
          "tier": 3.0
        },
        "clans": [
          {"id": "clan-1", "baseLevel": 1.0},
          {"id": "clan-2", "baseLevel": 2.0}
        ]
      }
    """.trimIndent()
    val element = json.parseToJsonElement(raw).normalizeConvexNumbers()
    val bandit = element.jsonObject["bandit"]!!.jsonObject
    assertEquals(1L, bandit["id"]!!.jsonPrimitive.longOrNull)
    assertEquals(60L, bandit["attackPower"]!!.jsonPrimitive.longOrNull)
    assertEquals(3L, bandit["tier"]!!.jsonPrimitive.longOrNull)
  }

  @Test
  fun decodeWorldSnapshotWithFloatIntegers() {
    // Replicates the v2.4.0 Hearth error payload from issue #229.
    val raw = """
      {
        "tick": 12.0,
        "activeBanditId": 1.0,
        "bandit": {
          "attackPower": 60.0,
          "id": 1.0,
          "nextActionTick": 12.0,
          "projectedTargetClanId": 0.0,
          "region": 4.0,
          "state": 1.0,
          "stateEnteredTick": 11.0,
          "tier": 3.0
        },
        "clans": [
          {
            "id": "clan-1",
            "blueprintBalance": "0",
            "goldBalance": "1000000000000000000",
            "name": "Sample Clan"
          }
        ],
        "seasonStartTick": 0.0,
        "seasonEndTick": 100.0,
        "winterActive": false
      }
    """.trimIndent()
    val element = json.parseToJsonElement(raw).normalizeConvexNumbers()
    val snapshot = json.decodeFromJsonElement<WorldSnapshot>(element)
    assertEquals(12L, snapshot.tick)
    assertEquals(1, snapshot.activeBanditId)
    assertNotNull(snapshot.bandit)
    assertEquals(1, snapshot.bandit!!.id)
    assertEquals(60, snapshot.bandit!!.attackPower)
    assertEquals(3, snapshot.bandit!!.tier)
    assertEquals(12L, snapshot.bandit!!.nextActionTick)
    assertEquals(0L, snapshot.seasonStartTick)
    assertEquals(100L, snapshot.seasonEndTick)
    assertEquals(1, snapshot.clans.size)
    assertEquals("Sample Clan", snapshot.clans[0].name)
  }

  @Test
  fun normalizeLeavesNonWholeNumbersAlone() {
    val raw = """[1.0, 1.5, 2.0, 2.7, 3]"""
    val element = json.parseToJsonElement(raw).normalizeConvexNumbers()
    val arr = (element as kotlinx.serialization.json.JsonArray).map { it.jsonPrimitive }
    assertEquals(1L, arr[0].longOrNull)
    assertNull(arr[1].longOrNull)
    assertEquals(2L, arr[2].longOrNull)
    assertNull(arr[3].longOrNull)
    assertEquals(3L, arr[4].longOrNull)
  }

  @Test
  fun normalizePreservesBooleansAndNulls() {
    val raw = """{"flag":true,"missing":null,"count":0.0}"""
    val element = json.parseToJsonElement(raw).normalizeConvexNumbers()
    assertEquals("true", element.jsonObject["flag"]!!.jsonPrimitive.content)
    assertEquals(JsonNull, element.jsonObject["missing"])
    assertEquals(0L, element.jsonObject["count"]!!.jsonPrimitive.longOrNull)
  }
}
