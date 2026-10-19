# Xoyrify — come compilare il .jar

Questo è un mod **Fabric** per Minecraft **1.20.1** che fa due cose:

1. **Downloader in-game**: tasto `M` apre una schermata di ricerca che
   interroga l'API pubblica di **Modrinth**, mostra i risultati e scarica
   la build giusta per la tua versione/mod loader direttamente nella
   cartella `mods/`, esattamente come fa Resourcify. Dopo il download ti
   avvisa che serve un riavvio del gioco per caricare la nuova mod.
2. **HUD personalizzabile**: tasto `N` apre un editor dove puoi trascinare
   ogni elemento (FPS, coordinate, bioma, orario, memoria, ping), accenderlo
   o spegnerlo, cambiargli colore, scala e sfondo. Le impostazioni si
   salvano in `config/xoyrify/hud.json`.

Non ho potuto compilare il `.jar` finale in questa sandbox perché l'ambiente
non ha accesso ai server Maven di Fabric/Mojang necessari a Gradle per
scaricare Minecraft, le mappature e Fabric API. Il codice è però completo:
basta buildarlo sul tuo computer.

## Requisiti

- **Java 17 o superiore** (JDK, non solo JRE) — [Adoptium](https://adoptium.net/)
- Connessione a internet (Gradle scarica Minecraft/Fabric al primo build)
- Non serve installare Gradle: il progetto include il wrapper (`gradlew`)

## Passaggi

```bash
cd xoyrify

# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

La prima build richiede qualche minuto (scarica Minecraft, le mappature Yarn
e Fabric API). Al termine trovi il jar compilato in:

```
build/libs/xoyrify-1.0.0.jar
```

## Installazione

1. Installa **Fabric Loader** per Minecraft 1.20.1 (fabricmc.net/use).
2. Scarica **Fabric API** 1.20.1 da Modrinth e mettila in `.minecraft/mods/`.
3. Copia `xoyrify-1.0.0.jar` nella stessa cartella `mods/`.
4. Avvia il profilo "fabric-loader-1.20.1" dal launcher.

## Tasti di default

- `M` → apri il downloader di mod
- `N` → apri l'editor della HUD

Sono rebindabili da Opzioni → Controlli → Xoyrify.

## Struttura del progetto

```
src/main/java/com/xoyrify/client/
├── XoyrifyClient.java          entry point, keybind, hook HUD
├── config/
│   ├── HudConfig.java         carica/salva config/xoyrify/hud.json
│   └── HudElementConfig.java  posizione, colore, scala, on/off per elemento
├── download/
│   ├── ModrinthClient.java    chiamate HTTP all'API Modrinth + download file
│   └── ModrinthProject.java   modello di una mod trovata
└── gui/
    ├── ModDownloaderScreen.java  ricerca + lista + download
    └── HudEditorScreen.java     editor drag&drop della HUD
```

## Possibili estensioni

- Aggiungere CurseForge come seconda fonte (serve una API key personale).
- Verifica hash SHA1 del file scaricato prima di sostituirlo.
- Rilevamento automatico delle dipendenze richieste da una mod (es. Fabric API).
- Elementi HUD aggiuntivi (direzione, armatura, fame) — basta aggiungere una
  voce in `HudElements.java`.
