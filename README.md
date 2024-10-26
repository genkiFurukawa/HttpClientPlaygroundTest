# HttpClientPlaygroundTest

## 概要

- `java.net.http.HttpClient`の`timeout`が`mockwebserver`だと効いていないように見えたので、調査した。
- 切り分けの一環として、`mock-server`も使って動作確認をした。

## 結論

- `setBodyDelay`ではなく、**`setHeadersDelay`**を使う。バージョンによっては**`setHeadersDelay`**がないので、必要に応じてバージョンをあげる。
- 