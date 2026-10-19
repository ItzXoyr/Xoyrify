# Xoyrify

Mod client-only per Minecraft **1.20.1** (Fabric) con due funzioni principali:

- **Downloader in-game**: tasto `M` apre una schermata di ricerca che
  interroga l'API pubblica di [Modrinth](https://modrinth.com), mostra i
  risultati e scarica la build compatibile con la tua versione/mod loader
  direttamente nella cartella `mods/`, come fa Resourcify.
- **HUD personalizzabile**: tasto `N` apre un editor dove trascinare ogni
  elemento (FPS, coordinate, bioma, orario, memoria, ping), accenderlo o
  spegnerlo, cambiargli colore, scala e sfondo. Le impostazioni si salvano
  in `config/xoyrify/hud.json`.

## Scaricare il jar già compilato

Ogni push su questo repository fa partire una **GitHub Action** che compila
il mod in automatico. Per scaricare l'ultimo build:

1. Vai sulla tab **Actions** di questo repository
2. Apri l'ultima esecuzione del workflow "Build mod" (quella con la spunta verde)
3. In fondo alla pagina, sotto "Artifacts", scarica **xoyrify-mod**
4. Estrai lo zip: dentro trovi `xoyrify-1.0.0.jar`

## Compilare in locale

```bash
./gradlew build        # Windows: gradlew.bat build
```

Il jar compilato esce in `build/libs/xoyrify-1.0.0.jar`.

## Installazione

1. Installa [Fabric Loader](https://fabricmc.net/use) per Minecraft 1.20.1
2. Scarica [Fabric API](https://modrinth.com/mod/fabric-api) per 1.20.1
3. Metti entrambi i jar (Fabric API + Xoyrify) in `.minecraft/mods/`
4. Avvia il profilo "fabric-loader-1.20.1"

Dettagli ed estensioni possibili in [COME_COMPILARE.md](COME_COMPILARE.md).
