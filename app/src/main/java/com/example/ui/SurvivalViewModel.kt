package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SurvivalDatabase
import com.example.data.model.CampStructureEntity
import com.example.data.model.ChestItemEntity
import com.example.data.model.ForestZone
import com.example.data.model.InventoryItemEntity
import com.example.data.model.ItemCategory
import com.example.data.model.ItemType
import com.example.data.model.JournalEntryEntity
import com.example.data.model.StructureType
import com.example.data.model.SurvivalProfile
import com.example.data.model.WeatherType
import com.example.data.repository.InventoryRepository
import com.example.data.repository.SurvivalRepository
import com.example.domain.CraftingCatalog
import com.example.domain.CraftingRecipe
import com.example.domain.OpenWorldManager
import com.example.domain.StructureRecipe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SurvivalViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: SurvivalRepository
  val inventoryRepository: InventoryRepository
  private val vibrator: Vibrator?
  val worldManager = OpenWorldManager()

  private val _activeDialog = MutableStateFlow(ActiveScreenDialog.NONE)
  private val _selectedCraftingCategory = MutableStateFlow("TODAS")
  private val _isSwingingTool = MutableStateFlow(false)
  private val _swingProgress = MutableStateFlow(0f)
  private val _isCrafting = MutableStateFlow(false)
  private val _craftingProgress = MutableStateFlow(0f)
  private val _floatingMessages = MutableStateFlow<List<FloatingLootMessage>>(emptyList())
  private val _activeScreenShake = MutableStateFlow(false)
  private val _bannerNotification = MutableStateFlow<String?>(null)

  // Open-World Player State
  private val _playerX = MutableStateFlow(OpenWorldManager.BASE_CENTER_X)
  private val _playerY = MutableStateFlow(OpenWorldManager.BASE_CENTER_Y + 30f)
  private val _playerFacingAngle = MutableStateFlow(0f)
  private val _selectedBuildingToPlace = MutableStateFlow<StructureType?>(null)
  private val _placementX = MutableStateFlow(OpenWorldManager.BASE_CENTER_X + 60f)
  private val _placementY = MutableStateFlow(OpenWorldManager.BASE_CENTER_Y)
  private val _canPlaceHere = MutableStateFlow(true)

  private var joystickDx = 0f
  private var joystickDy = 0f

  init {
    val db = SurvivalDatabase.getDatabase(application)
    repository = SurvivalRepository(db.survivalDao())
    inventoryRepository = InventoryRepository(db.survivalDao())

    vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      vibratorManager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    viewModelScope.launch {
      repository.initializeGameIfNeeded()
      startWorldSimulations()
    }
  }

  val uiState: StateFlow<SurvivalUiState> = combine(
    repository.profile,
    repository.inventory,
    repository.structures,
    repository.chestItems,
    repository.journalEntries,
    _activeDialog,
    _selectedCraftingCategory,
    _isSwingingTool,
    _swingProgress,
    _isCrafting,
    _craftingProgress,
    _floatingMessages,
    _activeScreenShake,
    _bannerNotification,
    _playerX,
    _playerY,
    _playerFacingAngle,
    _selectedBuildingToPlace,
    _placementX,
    _placementY,
    _canPlaceHere
  ) { args: Array<Any?> ->
    val profile = (args[0] as? SurvivalProfile) ?: SurvivalProfile()
    val inventory = (args[1] as? List<*>)?.filterIsInstance<InventoryItemEntity>() ?: emptyList()
    val structures = (args[2] as? List<*>)?.filterIsInstance<CampStructureEntity>() ?: emptyList()
    val chestItems = (args[3] as? List<*>)?.filterIsInstance<ChestItemEntity>() ?: emptyList()
    val journalEntries = (args[4] as? List<*>)?.filterIsInstance<JournalEntryEntity>() ?: emptyList()
    val activeDialog = (args[5] as? ActiveScreenDialog) ?: ActiveScreenDialog.NONE
    val selectedCategory = (args[6] as? String) ?: "TODAS"
    val isSwinging = (args[7] as? Boolean) ?: false
    val swingProgress = (args[8] as? Float) ?: 0f
    val isCrafting = (args[9] as? Boolean) ?: false
    val craftingProgress = (args[10] as? Float) ?: 0f
    val floatingMessages = (args[11] as? List<*>)?.filterIsInstance<FloatingLootMessage>() ?: emptyList()
    val screenShake = (args[12] as? Boolean) ?: false
    val bannerNotification = args[13] as? String
    val px = (args[14] as? Float) ?: OpenWorldManager.BASE_CENTER_X
    val py = (args[15] as? Float) ?: OpenWorldManager.BASE_CENTER_Y
    val facingAngle = (args[16] as? Float) ?: 0f
    val buildingToPlace = args[17] as? StructureType
    val placeX = (args[18] as? Float) ?: px
    val placeY = (args[19] as? Float) ?: py
    val canPlace = (args[20] as? Boolean) ?: true

    SurvivalUiState(
      profile = profile,
      inventory = inventory,
      structures = structures,
      chestItems = chestItems,
      journalEntries = journalEntries,
      activeDialog = activeDialog,
      selectedCraftingCategory = selectedCategory,
      isSwingingTool = isSwinging,
      swingProgress = swingProgress,
      isCrafting = isCrafting,
      craftingProgress = craftingProgress,
      floatingMessages = floatingMessages,
      activeScreenShake = screenShake,
      bannerNotification = bannerNotification,
      playerX = px,
      playerY = py,
      playerFacingAngle = facingAngle,
      selectedBuildingToPlace = buildingToPlace,
      placementX = placeX,
      placementY = placeY,
      canPlaceHere = canPlace
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SurvivalUiState()
  )

  private fun vibrate(durationMs: Long = 30) {
    runCatching {
      if (vibrator?.hasVibrator() == true) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
          @Suppress("DEPRECATION")
          vibrator.vibrate(durationMs)
        }
      }
    }
  }

  fun triggerLootMessage(text: String, isPositive: Boolean = true) {
    val message = FloatingLootMessage(text = text, isPositive = isPositive)
    _floatingMessages.value = _floatingMessages.value + message
    viewModelScope.launch {
      delay(2200)
      _floatingMessages.value = _floatingMessages.value.filter { it.id != message.id }
    }
  }

  fun showBanner(text: String) {
    _bannerNotification.value = text
    viewModelScope.launch {
      delay(3500)
      if (_bannerNotification.value == text) {
        _bannerNotification.value = null
      }
    }
  }

  private fun startWorldSimulations() {
    // 1. High frequency game loop (50ms = 20 FPS for player physics, arrows, and mobs)
    viewModelScope.launch {
      while (true) {
        delay(50L)
        tickPhysicsAndMobs()
      }
    }

    // 2. Slow metabolism and world time loop (1000ms = 1 in-game minute)
    viewModelScope.launch {
      while (true) {
        delay(1000L)
        simulateOneMinuteTick()
      }
    }
  }

  private fun tickPhysicsAndMobs() {
    val state = uiState.value
    if (state.profile.isGameOver) return

    // Update player position based on virtual joystick
    if (joystickDx != 0f || joystickDy != 0f) {
      val speed = if (state.profile.stamina > 10f) 5.5f else 3.0f
      var newX = (_playerX.value + joystickDx * speed).coerceIn(40f, OpenWorldManager.WORLD_WIDTH - 40f)
      var newY = (_playerY.value + joystickDy * speed).coerceIn(40f, OpenWorldManager.WORLD_HEIGHT - 40f)

      // Collision check with placed walls
      for (s in state.structures) {
        if (s.structureTypeId == "log_wall" || s.structureTypeId == "arrow_tower") {
          val dist = worldManager.distance(newX, newY, s.worldX, s.worldY)
          if (dist < 36f) {
            // Repel slightly
            newX = _playerX.value
            newY = _playerY.value
            break
          }
        }
      }

      _playerX.value = newX
      _playerY.value = newY

      // Update placement ghost position when moving
      if (_selectedBuildingToPlace.value != null) {
        _placementX.value = newX + cos(_playerFacingAngle.value) * 65f
        _placementY.value = newY + sin(_playerFacingAngle.value) * 65f
      }
    }

    // Update open-world mobs, defense towers, arrows, and drops
    worldManager.updateWorldSimulation(
      timeMinutes = state.profile.timeMinutes,
      isNight = state.isNight,
      playerX = _playerX.value,
      playerY = _playerY.value,
      structures = state.structures,
      onPlayerAttacked = { damage ->
        viewModelScope.launch {
          val newHealth = (state.profile.health - damage).coerceAtLeast(0f)
          var deathCause = state.profile.deathCause
          if (newHealth <= 0f) {
            deathCause = "Você foi derrotado pelas feras da noite."
          }
          repository.updateProfile(
            state.profile.copy(
              health = newHealth,
              isGameOver = newHealth <= 0f,
              deathCause = deathCause
            )
          )
          triggerLootMessage("Ataque de fera! -${damage.toInt()} Vida", isPositive = false)
          vibrate(120)
        }
      },
      onLootCollected = { itemType, amount ->
        addItemToInventory(itemType, amount)
        triggerLootMessage("Coletou +$amount ${itemType.displayName}")
        vibrate(25)
      }
    )
  }

  fun onJoystickMove(dx: Float, dy: Float, angle: Float) {
    joystickDx = dx
    joystickDy = dy
    if (dx != 0f || dy != 0f) {
      _playerFacingAngle.value = angle
    }
  }

  fun onActionAttack() {
    val state = uiState.value
    if (state.profile.isGameOver || _isSwingingTool.value) return

    val currentStamina = state.profile.stamina
    if (currentStamina < 6f) {
      triggerLootMessage("Estamina esgotada! Descanse um pouco.", isPositive = false)
      vibrate(80)
      return
    }

    vibrate(40)
    _isSwingingTool.value = true

    viewModelScope.launch {
      // Swing animation interpolation
      for (i in 1..6) {
        delay(35L)
        _swingProgress.value = i / 6f
      }
      _isSwingingTool.value = false
      _swingProgress.value = 0f
    }

    val hitSomething = worldManager.harvestNearestNode(
      playerX = _playerX.value,
      playerY = _playerY.value,
      equippedTool = state.equippedItem,
      onLoot = { itemType, amount ->
        addItemToInventory(itemType, amount)
        triggerLootMessage("+$amount ${itemType.displayName}")
        vibrate(30)
      }
    )

    viewModelScope.launch {
      val newStamina = (state.profile.stamina - 6f).coerceAtLeast(0f)
      repository.updateProfile(state.profile.copy(stamina = newStamina))
      if (hitSomething) {
        degradeEquippedTool()
      }
    }
  }

  // Base Building: Enter placement mode
  fun startPlacement(structureType: StructureType) {
    _selectedBuildingToPlace.value = structureType
    _placementX.value = _playerX.value + cos(_playerFacingAngle.value) * 65f
    _placementY.value = _playerY.value + sin(_playerFacingAngle.value) * 65f
    _activeDialog.value = ActiveScreenDialog.NONE
    showBanner("Modo Construção: Posicione ${structureType.displayName} e toque para erguer!")
  }

  fun confirmPlacement() {
    val structureType = _selectedBuildingToPlace.value ?: return
    val recipe = CraftingCatalog.STRUCTURE_RECIPES.firstOrNull { it.structureType == structureType } ?: return
    val state = uiState.value

    val hasIngredients = recipe.ingredients.all { ing ->
      state.getItemCount(ing.itemType) >= ing.amount
    }
    if (!hasIngredients) {
      triggerLootMessage("Materiais insuficientes para construir!", isPositive = false)
      vibrate(100)
      return
    }

    viewModelScope.launch {
      recipe.ingredients.forEach { ing ->
        deductItemFromInventory(ing.itemType, ing.amount)
      }

      val newStructure = CampStructureEntity(
        structureTypeId = structureType.id,
        worldX = _placementX.value,
        worldY = _placementY.value,
        health = structureType.maxHealth,
        level = 1,
        fuelMinutesRemaining = if (structureType == StructureType.FOGUEIRA) 90 else 0,
        waterStoredLiters = 0f,
        isBuilt = true,
        builtDay = state.profile.daysSurvived
      )
      repository.insertOrUpdateStructure(newStructure)

      triggerLootMessage("Construção concluída: ${structureType.displayName}!")
      vibrate(80)
      _selectedBuildingToPlace.value = null

      repository.addJournalEntry(
        JournalEntryEntity(
          day = state.profile.daysSurvived,
          timeString = state.formattedTime,
          title = "Fortificação: ${structureType.displayName}",
          message = "Ergui ${structureType.displayName} nas coordenadas (X:${_placementX.value.toInt()}, Y:${_placementY.value.toInt()}).",
          type = "MILESTONE"
        )
      )
    }
  }

  fun cancelPlacement() {
    _selectedBuildingToPlace.value = null
  }

  private suspend fun simulateOneMinuteTick() {
    val current = uiState.value.profile
    if (current.isGameOver) return

    val structures = uiState.value.structures
    val burningFires = structures.filter { it.structureTypeId == "campfire" && it.fuelMinutesRemaining > 0 }
    val rainCatcher = structures.firstOrNull { it.structureTypeId == "rain_catcher" }
    val hasLogCabin = structures.any { it.structureTypeId == "log_cabin" && it.isBuilt }
    val hasLeanTo = structures.any { it.structureTypeId == "lean_to" && it.isBuilt }

    // Advance time
    val newTimeMinutes = (current.timeMinutes + 1) % 1440
    val newDay = if (newTimeMinutes == 0) current.daysSurvived + 1 else current.daysSurvived

    if (newTimeMinutes == 0) {
      showBanner("🌅 Novo Dia $newDay na floresta! Feras retornaram às sombras.")
    } else if (newTimeMinutes == 1260) {
      showBanner("🌙 Anoiteceu! O frio aumenta e feras espreitam na escuridão. Fique perto da fogueira ou torres!")
    }

    // Weather random changes every ~3-4 hours
    var currentWeather = current.currentWeather
    if (newTimeMinutes % 210 == 0) {
      val roll = Random.nextInt(100)
      val newWeatherEnum = when {
        roll < 55 -> WeatherType.ENSOLARADO
        roll < 80 -> WeatherType.NEBLINA
        else -> WeatherType.TEMPESTADE_CHUVA
      }
      currentWeather = newWeatherEnum.name
      showBanner("Clima mudou: ${newWeatherEnum.displayName}")
    }

    val isRaining = currentWeather == WeatherType.TEMPESTADE_CHUVA.name
    val isNight = newTimeMinutes >= 1260 || newTimeMinutes < 360

    // Update burning campfires
    burningFires.forEach { fire ->
      val newFuel = (fire.fuelMinutesRemaining - 1).coerceAtLeast(0)
      repository.insertOrUpdateStructure(fire.copy(fuelMinutesRemaining = newFuel))
    }

    // Rain catcher filling
    if (rainCatcher != null && rainCatcher.isBuilt && isRaining) {
      val newWater = (rainCatcher.waterStoredLiters + 0.05f).coerceAtMost(10f)
      repository.insertOrUpdateStructure(rainCatcher.copy(waterStoredLiters = newWater))
    }

    // Hunger and Thirst decay
    val hungerDecay = 0.035f
    val thirstDecay = if (isRaining) 0.045f else 0.060f
    val newHunger = (current.hunger - hungerDecay).coerceAtLeast(0f)
    val newThirst = (current.thirst - thirstDecay).coerceAtLeast(0f)

    // Check proximity to any active burning fire in the open world
    val isNearActiveFire = burningFires.any {
      worldManager.distance(_playerX.value, _playerY.value, it.worldX, it.worldY) < 220f
    }

    // Wetness update
    var newWetness = current.wetness
    if (isRaining && !hasLogCabin) {
      newWetness = (newWetness + 0.8f).coerceAtMost(100f)
    } else if (isNearActiveFire) {
      newWetness = (newWetness - 3.0f).coerceAtLeast(0f)
    } else {
      newWetness = (newWetness - 0.2f).coerceAtLeast(0f)
    }

    // Body Temperature equilibrium calculation
    val baseAmbientTemp = when {
      isNight -> 8f
      newTimeMinutes in 1020..1259 -> 18f
      else -> 24f
    }
    val weatherOffset = when (currentWeather) {
      WeatherType.TEMPESTADE_CHUVA.name -> -6f
      WeatherType.NEBLINA.name -> -3f
      else -> 2f
    }
    var effectiveAmbient = baseAmbientTemp + weatherOffset

    if (isNearActiveFire) effectiveAmbient += 18f
    if (current.equippedItemId == ItemType.TORCH.id) effectiveAmbient += 5f
    if (current.equippedClothingId == ItemType.FUR_COAT.id) effectiveAmbient += 8f
    if (current.equippedClothingId == ItemType.LEATHER_BOOTS.id) effectiveAmbient += 3f

    // Chill factor from wetness
    effectiveAmbient -= (newWetness * 0.12f)

    val targetBodyTemp = when {
      effectiveAmbient >= 20f -> 36.8f
      effectiveAmbient in 10f..19f -> 36.2f
      effectiveAmbient in 0f..9f -> 35.2f
      else -> 34.2f
    }

    val tempDelta = (targetBodyTemp - current.bodyTemp) * 0.015f
    val newBodyTemp = (current.bodyTemp + tempDelta).coerceIn(33.5f, 38.5f)
    val isShivering = newBodyTemp < 35.5f

    // Health decay / regeneration
    var newHealth = current.health
    var deathReason: String? = null

    if (newBodyTemp < 34.6f) {
      newHealth -= 0.35f
      if (newHealth <= 0) deathReason = "Hipotermia extrema na floresta gelada."
    }
    if (newHunger <= 0f) {
      newHealth -= 0.25f
      if (newHealth <= 0) deathReason = "Inanição. Você não resistiu à fome."
    }
    if (newThirst <= 0f) {
      newHealth -= 0.40f
      if (newHealth <= 0) deathReason = "Desidratação severa. Faltou água pura."
    }

    if (current.hasInfection) {
      newHealth -= 0.15f
      if (newHealth <= 0) deathReason = "Infecção bacteriana não tratada."
    }

    if (newHealth > 0 && newHunger > 50f && newThirst > 50f && newBodyTemp >= 36.3f && !current.hasInfection) {
      newHealth = (newHealth + 0.15f).coerceAtMost(100f)
    }

    val staminaRegen = if (newHunger < 20f || newThirst < 20f || isShivering) 0.8f else 2.5f
    val newStamina = (current.stamina + staminaRegen).coerceAtMost(100f)

    val isGameOver = newHealth <= 0f

    val updatedProfile = current.copy(
      health = newHealth.coerceIn(0f, 100f),
      hunger = newHunger,
      thirst = newThirst,
      bodyTemp = newBodyTemp,
      stamina = newStamina,
      wetness = newWetness,
      isShivering = isShivering,
      daysSurvived = newDay,
      timeMinutes = newTimeMinutes,
      currentWeather = currentWeather,
      playerX = _playerX.value,
      playerY = _playerY.value,
      isGameOver = isGameOver,
      deathCause = deathReason
    )

    repository.updateProfile(updatedProfile)
  }

  private suspend fun degradeEquippedTool() {
    val state = uiState.value
    val equippedId = state.profile.equippedItemId ?: return
    val toolItem = state.inventory.firstOrNull { it.itemTypeId == equippedId } ?: return

    val broke = inventoryRepository.degradeTool(toolItem.id)
    if (broke) {
      repository.updateProfile(state.profile.copy(equippedItemId = null))
      triggerLootMessage("Sua ferramenta quebrou pelo desgaste!", isPositive = false)
      vibrate(200)
    }
  }

  fun addItemToInventory(itemType: ItemType, quantity: Int) {
    viewModelScope.launch {
      inventoryRepository.addCollectedResource(itemType, quantity)
    }
  }

  fun useItem(itemEntity: InventoryItemEntity) {
    val state = uiState.value
    val itemType = ItemType.values().firstOrNull { it.id == itemEntity.itemTypeId } ?: return

    viewModelScope.launch {
      var profile = state.profile

      when (itemType.category) {
        ItemCategory.ALIMENTO -> {
          var newHealth = (profile.health + itemType.healthRestored).coerceIn(0f, 100f)
          var newHunger = (profile.hunger + itemType.hungerRestored).coerceIn(0f, 100f)
          var newThirst = (profile.thirst + itemType.thirstRestored).coerceIn(0f, 100f)
          var newTemp = (profile.bodyTemp + itemType.tempEffect).coerceIn(33.5f, 38.5f)
          var hasInfection = profile.hasInfection

          if (itemType == ItemType.DIRTY_WATER) {
            if (Random.nextInt(100) < 40) {
              hasInfection = true
              triggerLootMessage("Você contraiu infecção da água do riacho!", isPositive = false)
            } else {
              triggerLootMessage("Bebeu água turva (+25 Sede)")
            }
          } else if (itemType == ItemType.RAW_MEAT) {
            if (Random.nextInt(100) < 50) {
              hasInfection = true
              triggerLootMessage("A carne crua causou cólicas severas!", isPositive = false)
            } else {
              triggerLootMessage("Comeu carne crua (+25 Fome)")
            }
          } else if (itemType == ItemType.HERBAL_TEA) {
            hasInfection = false
            triggerLootMessage("Chá quente acalmou o corpo (+1.8°C)")
          } else {
            triggerLootMessage("Consumiu ${itemType.displayName}")
          }

          profile = profile.copy(
            health = newHealth,
            hunger = newHunger,
            thirst = newThirst,
            bodyTemp = newTemp,
            hasInfection = hasInfection
          )
          consumeItemEntity(itemEntity, 1)
          vibrate(30)
        }

        ItemCategory.MEDICINA -> {
          var newHealth = (profile.health + itemType.healthRestored).coerceIn(0f, 100f)
          val cureInfection = itemType == ItemType.HERBAL_SALVE
          profile = profile.copy(
            health = newHealth,
            hasInfection = if (cureInfection) false else profile.hasInfection
          )
          triggerLootMessage("Tratou ferimentos (+${itemType.healthRestored.toInt()} Saúde)")
          consumeItemEntity(itemEntity, 1)
          vibrate(40)
        }

        ItemCategory.FERRAMENTA, ItemCategory.SOBREVIVENCIA -> {
          val newEquipped = if (profile.equippedItemId == itemType.id) null else itemType.id
          profile = profile.copy(equippedItemId = newEquipped)
          inventoryRepository.setEquippedState(itemEntity.id, newEquipped != null)
          val actionWord = if (newEquipped != null) "Equipou" else "Desequipou"
          triggerLootMessage("$actionWord ${itemType.displayName}")
          vibrate(25)
        }

        ItemCategory.VESTUARIO -> {
          val newClothing = if (profile.equippedClothingId == itemType.id) null else itemType.id
          profile = profile.copy(equippedClothingId = newClothing)
          inventoryRepository.setEquippedState(itemEntity.id, newClothing != null)
          val actionWord = if (newClothing != null) "Vestiu" else "Tirou"
          triggerLootMessage("$actionWord ${itemType.displayName}")
          vibrate(25)
        }

        else -> {
          triggerLootMessage("${itemType.displayName} é um recurso para fabricação.")
        }
      }

      repository.updateProfile(profile)
    }
  }

  private suspend fun consumeItemEntity(entity: InventoryItemEntity, count: Int = 1) {
    inventoryRepository.consumeOrDrop(entity, count)
  }

  fun craftItem(recipe: CraftingRecipe) {
    val state = uiState.value
    if (_isCrafting.value) return

    if (recipe.requiresWorkbench && !state.hasWorkbench) {
      triggerLootMessage("É necessário ter uma Bancada construída!", isPositive = false)
      vibrate(100)
      return
    }
    if (recipe.requiresCampfire && !state.hasCampfireBurning) {
      triggerLootMessage("É necessário ter uma Fogueira acesa!", isPositive = false)
      vibrate(100)
      return
    }

    val hasIngredients = recipe.ingredients.all { ing ->
      state.getItemCount(ing.itemType) >= ing.amount
    }
    if (!hasIngredients) {
      triggerLootMessage("Materiais insuficientes para fabricar!", isPositive = false)
      vibrate(100)
      return
    }

    _isCrafting.value = true
    _craftingProgress.value = 0f

    viewModelScope.launch {
      val steps = 10
      for (i in 1..steps) {
        delay(recipe.craftingTimeSeconds * 80L)
        _craftingProgress.value = i / steps.toFloat()
      }

      recipe.ingredients.forEach { ing ->
        deductItemFromInventory(ing.itemType, ing.amount)
      }

      inventoryRepository.addCraftedTool(recipe.resultItem, recipe.resultAmount)
      triggerLootMessage("Fabricado no Inventário: ${recipe.resultAmount}x ${recipe.resultItem.displayName}!")
      vibrate(60)

      val profile = uiState.value.profile
      repository.updateProfile(profile.copy(totalItemsCrafted = profile.totalItemsCrafted + 1))

      _isCrafting.value = false
      _craftingProgress.value = 0f
    }
  }

  fun buildStructure(recipe: StructureRecipe) {
    startPlacement(recipe.structureType)
  }

  private suspend fun deductItemFromInventory(itemType: ItemType, amount: Int) {
    var remaining = amount
    val inventory = uiState.value.inventory
    for (item in inventory) {
      if (item.itemTypeId == itemType.id) {
        if (item.quantity <= remaining) {
          remaining -= item.quantity
          repository.deleteInventoryItem(item.id)
        } else {
          repository.updateInventoryItem(item.copy(quantity = item.quantity - remaining))
          remaining = 0
        }
        if (remaining <= 0) break
      }
    }
  }

  fun addFuelToCampfire(fuelItem: ItemType) {
    val state = uiState.value
    if (state.getItemCount(fuelItem) < 1) {
      triggerLootMessage("Você não tem ${fuelItem.displayName} no inventário!", isPositive = false)
      return
    }

    val addedMinutes = when (fuelItem) {
      ItemType.STICK -> 20
      ItemType.WOOD -> 60
      ItemType.RESIN -> 40
      ItemType.CHARCOAL -> 45
      else -> 15
    }

    viewModelScope.launch {
      deductItemFromInventory(fuelItem, 1)
      val campfire = state.structures.firstOrNull { it.structureTypeId == "campfire" }
        ?: CampStructureEntity(structureTypeId = "campfire", worldX = _playerX.value, worldY = _playerY.value, health = 100f, fuelMinutesRemaining = 0)

      val updated = campfire.copy(
        fuelMinutesRemaining = (campfire.fuelMinutesRemaining + addedMinutes).coerceAtMost(360)
      )
      repository.insertOrUpdateStructure(updated)
      triggerLootMessage("Fogo alimentado! +$addedMinutes min de queima.")
      vibrate(30)
    }
  }

  fun drinkFromRainCatcher() {
    val state = uiState.value
    val collector = state.structures.firstOrNull { it.structureTypeId == "rain_catcher" }
    if (collector == null || collector.waterStoredLiters < 0.5f) {
      triggerLootMessage("Coletor de chuva vazio! Espere a próxima chuva.", isPositive = false)
      return
    }

    viewModelScope.launch {
      val newWater = (collector.waterStoredLiters - 0.5f).coerceAtLeast(0f)
      repository.insertOrUpdateStructure(collector.copy(waterStoredLiters = newWater))

      val profile = state.profile
      val updatedProfile = profile.copy(
        thirst = (profile.thirst + 45f).coerceAtMost(100f),
        health = (profile.health + 4f).coerceAtMost(100f)
      )
      repository.updateProfile(updatedProfile)
      triggerLootMessage("Bebeu água pura do coletor (+45 Sede)")
      vibrate(30)
    }
  }

  fun sleep(hours: Int) {
    val state = uiState.value
    if (state.profile.isGameOver) return

    val advanceMinutes = hours * 60
    val profile = state.profile

    viewModelScope.launch {
      showBanner("Dormindo por $hours horas no acampamento...")
      delay(1000)

      val newTime = (profile.timeMinutes + advanceMinutes) % 1440
      val dayIncrement = (profile.timeMinutes + advanceMinutes) / 1440
      val newDay = profile.daysSurvived + dayIncrement

      val hungerDrained = hours * 4.0f
      val thirstDrained = hours * 5.5f

      val updatedProfile = profile.copy(
        timeMinutes = newTime,
        daysSurvived = newDay,
        stamina = 100f,
        health = (profile.health + hours * 8f).coerceAtMost(100f),
        hunger = (profile.hunger - hungerDrained).coerceAtLeast(0f),
        thirst = (profile.thirst - thirstDrained).coerceAtLeast(0f),
        bodyTemp = 36.8f,
        wetness = 0f
      )

      repository.updateProfile(updatedProfile)
      triggerLootMessage("Acordou renovado! Estamina 100%.")
      vibrate(60)

      _activeDialog.value = ActiveScreenDialog.NONE
    }
  }

  fun restartGame() {
    viewModelScope.launch {
      worldManager.generateWorld()
      repository.resetToNewGame()
      _playerX.value = OpenWorldManager.BASE_CENTER_X
      _playerY.value = OpenWorldManager.BASE_CENTER_Y + 30f
      _activeDialog.value = ActiveScreenDialog.NONE
      showBanner("Nova jornada iniciada na floresta!")
    }
  }

  fun setDialog(dialog: ActiveScreenDialog) {
    _activeDialog.value = dialog
  }

  fun setSelectedCategory(category: String) {
    _selectedCraftingCategory.value = category
  }

  fun dropItem(item: InventoryItemEntity) {
    viewModelScope.launch {
      inventoryRepository.consumeOrDrop(item, item.quantity)
      triggerLootMessage("Descartou ${item.name} do inventário")
    }
  }
}
