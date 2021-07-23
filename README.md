# NFTLab smart contract in Hotmoka

[![Build](https://github.com/NFT-Lab/ERC721-marketplace-hotmoka/actions/workflows/build.yml/badge.svg)](https://github.com/NFT-Lab/smart-contract-hotmoka/actions/workflows/build.yml)
[![Code formatting](https://github.com/NFT-Lab/ERC721-marketplace-hotmoka/actions/workflows/code-formatting.yml/badge.svg)](https://github.com/NFT-Lab/smart-contract-hotmoka/actions/workflows/code-formatting.yml)

La seguente repository contiene lo smart contract per Hotmoka per la gestione della compra vendita di NFT nella piattaforma NFTLab.

## Strumenti utilizzati

* **[Maven](https://maven.apache.org/).**
* **[JUnit5](https://junit.org/junit5/):** libreria per la scrittura di test nel linguaggio java.

## Prerequisiti

1. Creare un token personale che permetta la lettura di pacchetti da github ([la guida qui](https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token)).

2. Creare il file **settings.xml** dentro la cartella **~/.m2** con il seguente contenuto:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
						http://maven.apache.org/xsd/settings-1.0.0.xsd">

	<activeProfiles>
	<activeProfile>github</activeProfile>
	</activeProfiles>

	<profiles>
	<profile>
		<id>github</id>
		<repositories>
		<repository>
			<id>central</id>
			<url>https://repo1.maven.org/maven2</url>
		</repository>
		<repository>
			<id>github</id>
			<url>https://maven.pkg.github.com/NFT-Lab/*</url>
			<snapshots>
			<enabled>true</enabled>
			</snapshots>
		</repository>
		</repositories>
	</profile>
	</profiles>

	<servers>
	<server>
		<id>github</id>
		<username>$GITHUB_USERNAME</username>
		<password>$ACCESS_TOKEN</password>
	</server>
	</servers>
</settings>
```

In questo modo si permetterà a maven di accedere alla repository maven di github.

3. Eseguire i seguenti comandi per installare Hotmoka 1.0.0 e le dipendenze necessarie alla compilazione del progetto 
   all'interno della cartella del progetto stesso:

```bash
$ git clone https://github.com/Hotmoka/hotmoka.git
$ cd hotmoka
$ git checkout 1.0.0
$ cd ..; mvn install -f hotmoka/pom.xml --projects io-hotmoka-takamaka
```