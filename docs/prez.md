---
title:
- modradio - Muxowanie HLS za pomocą libav
author:
- Grzegorz Koperwas
theme:
- Copenhagen
---

# Opis problemu:

Strona `modarchive.org` są udostępniane w następującej formie:

- Pojedyncze piosenki są przechowywane w plikach `zip`

- Piosenki znajdują się w skomplikowanym drzewie folderów dzielącym je ze względu
na rok dodania do archiwum oraz format.

- Istniejące programy takie jak `VLC` nie radzą sobie z losowym odtwarzaniem tej 
struktury folderów, oraz rozmiar około 40gb nie pozwala na jej modyfikację.

Celem projektu jest umożliwienie odtwarzania piosenek zdalnie, w kolejności prawdziwie
losowej.

# Rozwiązanie:

Przyjęte rozwiązanie zakłada łączenie w czasie rzeczywistym kolejnych plików w jeden
strumień **HLS**.

## HTTP Live Streaming:

**HLS** jest standardem strumieniowania multimediów opracowanym przez firmę Apple
na potrzeby ich własnych prezentacji. Warstwa transportowa protokołu polega wyłącznie
na hostowaniu plików statycznych `.ts` oraz pliku tekstowego *drogowskazu* `.m3u8`.

# Opis programu:

- Program przyjmuje przez `stdin` ścieżki do plików.
    - Jeżeli plik nie jest poprawny, jest on pomijany
    - Program stara się mieć dwa pliki otwarte na raz.
- Pliki te są demuxowane z formatów takich jak `.mp3`, `.mp4` czy `.aac`:
    - Jeżeli plik zawiera wiele strumieni audio, to zostaje wybrany pierwszy.
    - Inne dane są ignorowane.
- Strumień audio jest dekodowany, resamplowany do odpowiedniego formatu i częstotliwości 
i kodowany do `AAC`. Kolejne pliki są łączone w jeden strumień.
- Zakodowany strumień jest przekazywany do muxera **HLS**.

# Rekomendowane użycie:

Program jest przewidziany jako część większego rozwiązania składającego się z:

- Skryptu dostarczającego kolejne pliki do zmuxowania.
- Serwera http, do hostowania strumienia **HLS**.

# Demo

DEMO

# Wnioski

- `libav` jest używalna z poziomu języka `java`. Poznałem lepiej jej architekturę,
wcześniej wykorzystywałem ją w `c++` oraz za pomocą narzędzia `ffmpeg` w pythonie.

