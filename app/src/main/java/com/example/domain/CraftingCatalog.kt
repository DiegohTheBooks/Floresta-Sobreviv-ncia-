package com.example.domain

import com.example.data.model.ItemCategory
import com.example.data.model.ItemType
import com.example.data.model.StructureType

data class CraftingIngredient(
  val itemType: ItemType,
  val amount: Int
)

data class CraftingRecipe(
  val id: String,
  val resultItem: ItemType,
  val resultAmount: Int = 1,
  val category: ItemCategory,
  val ingredients: List<CraftingIngredient>,
  val requiresWorkbench: Boolean = false,
  val requiresCampfire: Boolean = false,
  val craftingTimeSeconds: Int = 2,
  val description: String
)

data class StructureRecipe(
  val structureType: StructureType,
  val ingredients: List<CraftingIngredient>,
  val requiresWorkbench: Boolean = false,
  val buildTimeSeconds: Int = 4,
  val perkDescription: String
)

object CraftingCatalog {
  val RECIPES: List<CraftingRecipe> = listOf(
    // === FERRAMENTAS & ARMAS BÁSICAS (Improvisadas) ===
    CraftingRecipe(
      id = "craft_axe_stone",
      resultItem = ItemType.AXE_STONE,
      category = ItemCategory.FERRAMENTA,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 2),
        CraftingIngredient(ItemType.STICK, 3),
        CraftingIngredient(ItemType.STONE, 3),
        CraftingIngredient(ItemType.FIBER, 4)
      ),
      description = "Machado de corte afiado para derrubar árvores e coletar madeira pesada."
    ),
    CraftingRecipe(
      id = "craft_knife_stone",
      resultItem = ItemType.KNIFE_STONE,
      category = ItemCategory.FERRAMENTA,
      ingredients = listOf(
        CraftingIngredient(ItemType.STICK, 2),
        CraftingIngredient(ItemType.FLINT, 2),
        CraftingIngredient(ItemType.FIBER, 2)
      ),
      description = "Faca de precisão para esfolar presas e extrair cipós e fibras."
    ),
    CraftingRecipe(
      id = "craft_pickaxe_flint",
      resultItem = ItemType.PICKAXE_FLINT,
      category = ItemCategory.FERRAMENTA,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 2),
        CraftingIngredient(ItemType.STICK, 3),
        CraftingIngredient(ItemType.FLINT, 4),
        CraftingIngredient(ItemType.FIBER, 4)
      ),
      description = "Picareta de sílex para fraturar veios de pedra e colher minerais."
    ),
    CraftingRecipe(
      id = "craft_spear_wood",
      resultItem = ItemType.SPEAR_WOOD,
      category = ItemCategory.FERRAMENTA,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 1),
        CraftingIngredient(ItemType.STICK, 4),
        CraftingIngredient(ItemType.FIBER, 3)
      ),
      description = "Lança longa de alcance para caça de animais rápidos e defesa."
    ),

    // === FERRAMENTAS AVANÇADAS (Exigem Bancada) ===
    CraftingRecipe(
      id = "craft_axe_steel",
      resultItem = ItemType.AXE_STEEL,
      category = ItemCategory.FERRAMENTA,
      requiresWorkbench = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 4),
        CraftingIngredient(ItemType.FLINT, 6),
        CraftingIngredient(ItemType.RESIN, 3),
        CraftingIngredient(ItemType.VINE, 4)
      ),
      description = "Machado forjado reforçado com resina e cipós. Alta durabilidade."
    ),
    CraftingRecipe(
      id = "craft_spear_flint",
      resultItem = ItemType.SPEAR_FLINT,
      category = ItemCategory.FERRAMENTA,
      requiresWorkbench = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 2),
        CraftingIngredient(ItemType.FLINT, 5),
        CraftingIngredient(ItemType.VINE, 3),
        CraftingIngredient(ItemType.RESIN, 2)
      ),
      description = "Lança de caça com ponta de sílex laminada, capaz de abater grandes cervos."
    ),
    CraftingRecipe(
      id = "craft_bow_hunting",
      resultItem = ItemType.BOW_HUNTING,
      category = ItemCategory.FERRAMENTA,
      requiresWorkbench = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 3),
        CraftingIngredient(ItemType.VINE, 5),
        CraftingIngredient(ItemType.FIBER, 6)
      ),
      description = "Arco curvado flexível para caça silenciosa a longa distância."
    ),
    CraftingRecipe(
      id = "craft_arrows",
      resultItem = ItemType.ARROW,
      resultAmount = 5,
      category = ItemCategory.FERRAMENTA,
      requiresWorkbench = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.STICK, 5),
        CraftingIngredient(ItemType.FLINT, 2),
        CraftingIngredient(ItemType.FIBER, 3)
      ),
      description = "Conjunto de 5 flechas de caça com pontas perfurantes."
    ),

    // === SOBREVIVÊNCIA & FOGO ===
    CraftingRecipe(
      id = "craft_fire_drill",
      resultItem = ItemType.FIRE_DRILL,
      category = ItemCategory.SOBREVIVENCIA,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 1),
        CraftingIngredient(ItemType.STICK, 3),
        CraftingIngredient(ItemType.FIBER, 2)
      ),
      description = "Broca de fricção de madeira para acender fogueiras sem fósforos."
    ),
    CraftingRecipe(
      id = "craft_torch",
      resultItem = ItemType.TORCH,
      resultAmount = 2,
      category = ItemCategory.SOBREVIVENCIA,
      ingredients = listOf(
        CraftingIngredient(ItemType.STICK, 3),
        CraftingIngredient(ItemType.RESIN, 2),
        CraftingIngredient(ItemType.FIBER, 2)
      ),
      description = "Tocha resinada que ilumina os arredores e afasta animais na escuridão."
    ),
    CraftingRecipe(
      id = "craft_canteen",
      resultItem = ItemType.LEATHER_CANTEEN,
      category = ItemCategory.SOBREVIVENCIA,
      ingredients = listOf(
        CraftingIngredient(ItemType.HIDE, 2),
        CraftingIngredient(ItemType.VINE, 2),
        CraftingIngredient(ItemType.RESIN, 1)
      ),
      description = "Cantil vedado com resina natural para carregar água potável fresca."
    ),

    // === CULINÁRIA (Exige Fogueira) ===
    CraftingRecipe(
      id = "cook_meat",
      resultItem = ItemType.COOKED_MEAT,
      category = ItemCategory.ALIMENTO,
      requiresCampfire = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.RAW_MEAT, 1),
        CraftingIngredient(ItemType.STICK, 1)
      ),
      description = "Carne fresca assada na brasa da fogueira. Segura e altamente nutritiva."
    ),
    CraftingRecipe(
      id = "boil_water",
      resultItem = ItemType.CLEAN_WATER,
      category = ItemCategory.ALIMENTO,
      requiresCampfire = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.DIRTY_WATER, 1),
        CraftingIngredient(ItemType.CHARCOAL, 1)
      ),
      description = "Ferva a água turva do riacho com carvão filtrante para torná-la 100% pura."
    ),
    CraftingRecipe(
      id = "brew_tea",
      resultItem = ItemType.HERBAL_TEA,
      category = ItemCategory.ALIMENTO,
      requiresCampfire = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.CLEAN_WATER, 1),
        CraftingIngredient(ItemType.HERB, 2)
      ),
      description = "Chá revigorante fervido com folhas silvestres. Aquece o corpo e cura enfermidades."
    ),

    // === MEDICINA ===
    CraftingRecipe(
      id = "craft_bandage",
      resultItem = ItemType.BANDAGE,
      resultAmount = 2,
      category = ItemCategory.MEDICINA,
      ingredients = listOf(
        CraftingIngredient(ItemType.FIBER, 4),
        CraftingIngredient(ItemType.HERB, 1)
      ),
      description = "Bandagem com compressa de ervas para estancar sangramentos e restaurar saúde."
    ),
    CraftingRecipe(
      id = "craft_salve",
      resultItem = ItemType.HERBAL_SALVE,
      category = ItemCategory.MEDICINA,
      requiresWorkbench = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.RESIN, 3),
        CraftingIngredient(ItemType.HERB, 3),
        CraftingIngredient(ItemType.CLAY, 1)
      ),
      description = "Bálsamo cicatrizante concentrado com efeito antimicrobiano potente."
    ),

    // === VESTUÁRIO & PROTEÇÃO TÉRMICA (Exige Bancada) ===
    CraftingRecipe(
      id = "craft_fur_coat",
      resultItem = ItemType.FUR_COAT,
      category = ItemCategory.VESTUARIO,
      requiresWorkbench = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.HIDE, 4),
        CraftingIngredient(ItemType.FIBER, 6),
        CraftingIngredient(ItemType.VINE, 2)
      ),
      description = "Casaco pesado de pele animal. Isola a perda de calor corporal (+2.5°C)."
    ),
    CraftingRecipe(
      id = "craft_boots",
      resultItem = ItemType.LEATHER_BOOTS,
      category = ItemCategory.VESTUARIO,
      requiresWorkbench = true,
      ingredients = listOf(
        CraftingIngredient(ItemType.HIDE, 2),
        CraftingIngredient(ItemType.RESIN, 2),
        CraftingIngredient(ItemType.FIBER, 4)
      ),
      description = "Botas impermeabilizadas com resina para evitar umidade e picadas na mata."
    )
  )

  val STRUCTURE_RECIPES: List<StructureRecipe> = listOf(
    StructureRecipe(
      structureType = StructureType.FOGUEIRA,
      ingredients = listOf(
        CraftingIngredient(ItemType.STONE, 8),
        CraftingIngredient(ItemType.STICK, 6),
        CraftingIngredient(ItemType.WOOD, 2)
      ),
      perkDescription = "Emite calor corporal num raio seguro, ilumina à noite e permite assar alimentos e ferver água."
    ),
    StructureRecipe(
      structureType = StructureType.MURALHA_TRONCOS,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 6),
        CraftingIngredient(ItemType.VINE, 3)
      ),
      perkDescription = "Barricada defensiva estilo Krafteers para cercar e proteger sua base contra invasões noturnas."
    ),
    StructureRecipe(
      structureType = StructureType.TORRE_FLECHAS,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 10),
        CraftingIngredient(ItemType.STONE, 6),
        CraftingIngredient(ItemType.VINE, 4),
        CraftingIngredient(ItemType.FLINT, 4)
      ),
      requiresWorkbench = true,
      perkDescription = "Torre defensiva automática que dispara flechas certeiras em lobos e feras que invadem seu perímetro!"
    ),
    StructureRecipe(
      structureType = StructureType.ABRIGO_FOLHAS,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 6),
        CraftingIngredient(ItemType.STICK, 10),
        CraftingIngredient(ItemType.FIBER, 8)
      ),
      perkDescription = "Permite descansar para avançar o tempo e recuperar estamina. Protege parcialmente da brisa noturna."
    ),
    StructureRecipe(
      structureType = StructureType.CABANA_MADEIRA,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 20),
        CraftingIngredient(ItemType.CLAY, 6),
        CraftingIngredient(ItemType.VINE, 8),
        CraftingIngredient(ItemType.STONE, 10)
      ),
      requiresWorkbench = true,
      perkDescription = "Residência robusta selada com barro. Bloqueia chuva, vento e predadores, garantindo sono seguro e quente (+4°C)."
    ),
    StructureRecipe(
      structureType = StructureType.COLETOR_CHUVA,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 4),
        CraftingIngredient(ItemType.CLAY, 4),
        CraftingIngredient(ItemType.CHARCOAL, 3),
        CraftingIngredient(ItemType.FIBER, 6)
      ),
      perkDescription = "Capta e filtra água da chuva automaticamente durante tempestades, fornecendo água potável limpa."
    ),
    StructureRecipe(
      structureType = StructureType.BANCADA_TRABALHO,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 8),
        CraftingIngredient(ItemType.STONE, 4),
        CraftingIngredient(ItemType.VINE, 4)
      ),
      perkDescription = "Desbloqueia receitas avançadas de carpintaria: ferramentas forjadas, arco de caça e casacos de pele."
    ),
    StructureRecipe(
      structureType = StructureType.VARAL_SECAGEM,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 4),
        CraftingIngredient(ItemType.STICK, 8),
        CraftingIngredient(ItemType.VINE, 4)
      ),
      perkDescription = "Permite curar carne crua ao vento, transformando-a em carne seca defumada que não apodrece."
    ),
    StructureRecipe(
      structureType = StructureType.BAU_ARMAZENAMENTO,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 10),
        CraftingIngredient(ItemType.RESIN, 3),
        CraftingIngredient(ItemType.FIBER, 6)
      ),
      perkDescription = "Armazém seguro no acampamento para estocar toras, pedras, alimentos e itens sobressalentes."
    ),
    StructureRecipe(
      structureType = StructureType.CERCA_ESTACAS,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 12),
        CraftingIngredient(ItemType.VINE, 6)
      ),
      perkDescription = "Paliçada pontiaguda que afasta alcateias de lobos e javalis agressivos durante a noite."
    ),
    StructureRecipe(
      structureType = StructureType.CAMA_PELES,
      ingredients = listOf(
        CraftingIngredient(ItemType.WOOD, 4),
        CraftingIngredient(ItemType.HIDE, 4),
        CraftingIngredient(ItemType.FIBER, 8)
      ),
      requiresWorkbench = true,
      perkDescription = "Cama com colchão de peles fofas. Recupera 100% da saúde e estamina ao dormir durante a noite."
    )
  )
}
