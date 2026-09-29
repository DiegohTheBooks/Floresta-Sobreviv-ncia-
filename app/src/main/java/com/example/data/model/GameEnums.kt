package com.example.data.model

enum class ItemCategory(val displayName: String) {
  RECURSO("Recursos"),
  FERRAMENTA("Ferramentas"),
  ALIMENTO("Alimentos & Água"),
  SOBREVIVENCIA("Sobrevivência"),
  VESTUARIO("Vestuário & Abrigo"),
  MEDICINA("Medicina")
}

enum class ItemType(
  val id: String,
  val displayName: String,
  val category: ItemCategory,
  val maxStack: Int,
  val maxDurability: Int = 0,
  val hungerRestored: Float = 0f,
  val thirstRestored: Float = 0f,
  val healthRestored: Float = 0f,
  val tempEffect: Float = 0f,
  val description: String
) {
  // Recursos Básicos
  WOOD("wood", "Tronco de Madeira", ItemCategory.RECURSO, 60, description = "Tronco resistente cortado de árvores. Essencial para construir muralhas, torres e manter fogueiras acesas."),
  STICK("stick", "Graveto Seco", ItemCategory.RECURSO, 80, description = "Galho fino caído no chão. Ideal para acender fogo rápido, flechas e cabos de ferramentas."),
  STONE("stone", "Pedra Polida", ItemCategory.RECURSO, 60, description = "Pedra pesada coletada em encostas ou rios. Usada para ferramentas rústicas, fundações e torres."),
  FLINT("flint", "Sílex Lascado", ItemCategory.RECURSO, 40, description = "Pedra mineral muito cortante. Indispensável para produzir faíscas de fogo e pontas de flecha e ferramentas."),
  FIBER("fiber", "Fibra Vegetal", ItemCategory.RECURSO, 80, description = "Fios resistentes retirados de folhagens da mata. Usada para amarrações, cordas e arcos."),
  VINE("vine", "Cipó Forte", ItemCategory.RECURSO, 40, description = "Cipó elástico e grosso para estruturas pesadas e confecção de arcos."),
  HIDE("hide", "Pele de Animal", ItemCategory.RECURSO, 30, description = "Pele rústica obtida na caça. Proporciona isolamento térmico extremo contra o frio noturno."),
  RESIN("resin", "Resina de Pinheiro", ItemCategory.RECURSO, 40, description = "Substância pegajosa e inflamável extraída de troncos de pinheiro. Excelente impermeabilizante e combustível."),
  CHARCOAL("charcoal", "Carvão Vegetal", ItemCategory.RECURSO, 50, description = "Resíduo carbonizado de madeira queimada na fogueira. Vital para filtrar água e produzir calor."),
  CLAY("clay", "Argila da Margem", ItemCategory.RECURSO, 40, description = "Argila úmida colhida na margem do rio. Usada para fornos, panelas e vedação de cabanas."),

  // Alimentos e Água
  BERRIES("berries", "Frutas Silvestres", ItemCategory.ALIMENTO, 40, hungerRestored = 12f, thirstRestored = 8f, healthRestored = 2f, description = "Amoras e mirtilos selvagens doces. Aliviam levemente a fome e a sede sem riscos."),
  MUSHROOM("mushroom", "Cogumelo Comestível", ItemCategory.ALIMENTO, 30, hungerRestored = 16f, healthRestored = 3f, description = "Cogumelo seguro colhido em troncos caídos. Nutritivo e fácil de mastigar."),
  RAW_MEAT("raw_meat", "Carne Crua de Caça", ItemCategory.ALIMENTO, 20, hungerRestored = 25f, thirstRestored = -5f, healthRestored = -10f, description = "Carne crua de cervo ou javali. ATENÇÃO: Comer crua tem alto risco de parasitas! Deve ser cozida na fogueira."),
  COOKED_MEAT("cooked_meat", "Carne Assada Suculenta", ItemCategory.ALIMENTO, 25, hungerRestored = 55f, healthRestored = 15f, tempEffect = 0.5f, description = "Carne assada na brasa da fogueira. Fornece grande quantidade de proteínas e calor corporal."),
  DRIED_MEAT("dried_meat", "Carne Seca Defumada", ItemCategory.ALIMENTO, 40, hungerRestored = 40f, healthRestored = 8f, description = "Carne desidratada no canteiro de secagem. Não estraga e é ideal para longas expedições."),
  DIRTY_WATER("dirty_water", "Água do Riacho (Não Tratada)", ItemCategory.ALIMENTO, 10, thirstRestored = 25f, healthRestored = -15f, description = "Água coletada diretamente do rio. Pode conter bactérias da floresta! Ferva antes de beber."),
  CLEAN_WATER("clean_water", "Água Pura Fervida", ItemCategory.ALIMENTO, 10, thirstRestored = 45f, healthRestored = 5f, tempEffect = 0.4f, description = "Água cristalina purificada na fogueira ou pelo coletor de chuva. Hidratação 100% segura."),
  HERBAL_TEA("herbal_tea", "Chá de Ervas Quente", ItemCategory.ALIMENTO, 10, hungerRestored = 5f, thirstRestored = 40f, healthRestored = 18f, tempEffect = 1.8f, description = "Infusão curativa que aquece o corpo instantaneamente, restaurando saúde e combatendo hipotermia."),

  // Medicina
  HERB("herb", "Erva Medicinal Silvestre", ItemCategory.MEDICINA, 50, healthRestored = 6f, description = "Folhas com propriedades analgésicas e antissépticas encontradas na mata."),
  BANDAGE("bandage", "Bandagem Rústica", ItemCategory.MEDICINA, 20, healthRestored = 30f, description = "Faixa de fibras limpas para estancar sangramentos e tratar cortes de feras ou ferramentas."),
  HERBAL_SALVE("herbal_salve", "Pomada Cicatrizante", ItemCategory.MEDICINA, 15, healthRestored = 45f, description = "Mistura de resina, folhas medicinais e gordura animal para acelerar a regeneração celular."),

  // Ferramentas & Armas (com durabilidade)
  AXE_STONE("axe_stone", "Machado de Pedra", ItemCategory.FERRAMENTA, 1, maxDurability = 50, description = "Ferramenta básica de pedra lascada. Permite cortar árvores e coletar madeira rapidamente no mapa."),
  AXE_STEEL("axe_steel", "Machado de Aço Forjado", ItemCategory.FERRAMENTA, 1, maxDurability = 150, description = "Machado balanceado com lâmina forjada. Derruba árvores rapidamente com baixo consumo de estamina."),
  PICKAXE_FLINT("pickaxe_flint", "Picareta de Sílex", ItemCategory.FERRAMENTA, 1, maxDurability = 60, description = "Permite minerar pedregulhos, extrair sílex puro e minerais do solo florestal."),
  KNIFE_STONE("knife_stone", "Faca de Caça", ItemCategory.FERRAMENTA, 1, maxDurability = 70, description = "Lâmina afiada para esfolar caças, cortar cipós e esculpir gravetos."),
  SPEAR_WOOD("spear_wood", "Lança de Madeira Rústica", ItemCategory.FERRAMENTA, 1, maxDurability = 40, description = "Lança longa de alcance para caça de animais e combate corpo a corpo contra feras."),
  SPEAR_FLINT("spear_flint", "Lança de Caça com Sílex", ItemCategory.FERRAMENTA, 1, maxDurability = 80, description = "Arma perfurante letal para enfrentar lobos da noite, cervos e predadores."),
  BOW_HUNTING("bow_hunting", "Arco Curto de Caça", ItemCategory.FERRAMENTA, 1, maxDurability = 80, description = "Arco flexível entalhado em madeira nobre. Dispara flechas certeiras à distância."),
  ARROW("arrow", "Flecha com Pena e Sílex", ItemCategory.FERRAMENTA, 60, description = "Projétil de caça equilibrado com penas e sílex para arco e torres defensivas."),

  // Sobrevivência
  FIRE_DRILL("fire_drill", "Kit de Fricção para Fogo", ItemCategory.SOBREVIVENCIA, 5, maxDurability = 20, description = "Broca de graveto e tábua seca para gerar brasas por atrito. Cria fogo mesmo sem sílex."),
  TORCH("torch", "Tocha Resinada", ItemCategory.SOBREVIVENCIA, 5, maxDurability = 120, tempEffect = 1.0f, description = "Tocha empunhada que ilumina a escuridão da noite em 360°, aquece e afasta feras."),
  LEATHER_CANTEEN("leather_canteen", "Cantil de Couro", ItemCategory.SOBREVIVENCIA, 2, maxDurability = 100, description = "Recipiente hermético para transportar água potável durante a exploração do mapa."),

  // Vestuário & Proteção Térmica
  FUR_COAT("fur_coat", "Casaco Térmico de Peles", ItemCategory.VESTUARIO, 1, maxDurability = 200, tempEffect = 2.5f, description = "Casaco espesso forrado com peles de animais selvagens. Proteção essencial contra o frio rigoroso da noite e chuvas."),
  LEATHER_BOOTS("leather_boots", "Botas de Couro Reforçadas", ItemCategory.VESTUARIO, 1, maxDurability = 200, tempEffect = 1.2f, description = "Calçado resistente que isola a umidade do chão encharcado da floresta e aumenta velocidade.")
}

enum class StructureType(
  val id: String,
  val displayName: String,
  val description: String,
  val maxHealth: Float = 100f
) {
  FOGUEIRA(
    "campfire",
    "Fogueira de Pedra",
    "Ponto focal de calor e luz em 360°. Aquece o corpo, afasta feras da noite, cozinha alimentos e ferve água.",
    maxHealth = 120f
  ),
  MURALHA_TRONCOS(
    "log_wall",
    "Muralha de Troncos",
    "Barricada defensiva de alta resistência que bloqueia a passagem de predadores e forma sua fortaleza.",
    maxHealth = 250f
  ),
  TORRE_FLECHAS(
    "arrow_tower",
    "Torre de Flechas Defensiva",
    "Torre de vigilância estilo Krafteers que atira flechas automaticamente em lobos e monstros que se aproximam da base!",
    maxHealth = 200f
  ),
  ABRIGO_FOLHAS(
    "lean_to",
    "Abrigo de Folhas & Troncos",
    "Abrigo rústico para passar a noite. Permite descansar com segurança e avançar o tempo.",
    maxHealth = 120f
  ),
  CABANA_MADEIRA(
    "log_cabin",
    "Cabana de Troncos Reforçada",
    "Construção sólida com isolamento completo contra chuva e frio. Bônus de calor de +4°C ao dormir.",
    maxHealth = 350f
  ),
  COLETOR_CHUVA(
    "rain_catcher",
    "Coletor de Chuva com Filtro",
    "Tanque de captação que enche automaticamente de água potável limpa durante tempestades.",
    maxHealth = 100f
  ),
  BANCADA_TRABALHO(
    "workbench",
    "Bancada de Fabricação",
    "Estação de carpintaria que destrava ferramentas avançadas, arcos, torres e muralhas de madeira.",
    maxHealth = 150f
  ),
  VARAL_SECAGEM(
    "drying_rack",
    "Canteiro de Secagem de Carne",
    "Desidrata carnes cruas transformando-as em Carne Seca de longa duração sem precisar de fogo.",
    maxHealth = 100f
  ),
  BAU_ARMAZENAMENTO(
    "storage_chest",
    "Baú de Armazenamento",
    "Baú espaçoso para estocar pilhas de recursos e itens com segurança no acampamento.",
    maxHealth = 200f
  ),
  CERCA_ESTACAS(
    "palisade",
    "Cerca de Estacas Pontiagudas",
    "Fortificação defensiva perimetral com espinhos que causam dano a feras que colidem com ela.",
    maxHealth = 180f
  ),
  CAMA_PELES(
    "fur_bed",
    "Cama Macia de Peles",
    "Leito aconchegante que restaura toda a estamina e acelera a cura de ferimentos durante o sono.",
    maxHealth = 120f
  )
}

enum class WeatherType(val displayName: String, val ambientTempOffset: Float) {
  ENSOLARADO("Céu Aberto / Ensolarado", 2.0f),
  NEBLINA("Neblina Densa & Fria", -2.5f),
  TEMPESTADE_CHUVA("Tempestade de Chuva", -5.5f)
}

enum class ForestZone(val displayName: String, val description: String) {
  ACAMPAMENTO("Seu Acampamento", "Área clareada onde ficam suas construções, fogueira e suprimentos."),
  FLORESTA_PROFUNDA("Floresta Profunda", "Mata fechada com carvalhos centenários, rica em madeira, gravetos e caça."),
  RIACHO("Margem do Riacho", "Águas correntes ricas em seixos, argila fresca e peixes de água doce."),
  CLAREIRA_ROCHOSA("Clareira Rochosa", "Afloramento íngreme com grandes rochas de sílex, pedras e minerais.")
}
