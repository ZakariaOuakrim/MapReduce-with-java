# MapReduce - Total des Ventes par Ville

Ce projet contient un job MapReduce qui calcule le total des ventes par ville à partir d'un fichier texte (ventes.txt) contenant les données de vente d'une entreprise.

## Structure du Projet

- `TotalVentesParVille.java` : Code source du job MapReduce
- `pom.xml` : Configuration Maven pour la construction du projet
- `docker-compose.yml` : Configuration des services Hadoop (HDFS, YARN, etc.)
- `ventes.txt` : Fichier exemple de données de vente

## Format des Données

Le fichier d'entrée `data.txt` contient des lignes au format suivant :
```
date ville produit prix
```

Exemple :
```
2023-05-10 Paris Ordinateur 899.99
2023-05-10 Lyon Telephone 499.50
```

## Comment Exécuter le Projet

### 1. Configuration de l'Environnement

Assurez-vous d'avoir installé :
- Java JDK 8 ou supérieur
- Maven
- Docker et Docker Compose

### 2. Compilation du Projet

Dans IntelliJ IDEA :
1. Ouvrez le projet
2. Construisez-le via Maven (lifecycle > package)

Ou via la ligne de commande :
```bash
mvn clean package
```


### 3. Exécution du Job MapReduce

```bash
docker exec -it hadoop-client hadoop jar /app/hadoop-ventes-par-ville-1.0-SNAPSHOT.jar TotalVentesParVille /input/ventes.txt /output
```

### 4. Affichage des Résultats

```bash
docker exec -it hadoop-client hadoop fs -cat /output/part-r-00000
```

Les résultats afficheront chaque ville suivie du total des ventes, par exemple :
```
Lyon    854.47
Marseille    2099.98
Paris    1709.95
```

### 5. Interfaces Web

- NameNode HDFS : http://localhost:9870
- ResourceManager YARN : http://localhost:8088

### 6. Arrêt de l'Environnement

```bash
docker-compose down
```

## Fonctionnement du Code

1. **Mapper** (`VentesMapper`) :
   - Prend chaque ligne du fichier d'entrée
   - Parse les champs : date, ville, produit, prix
   - Émet des paires (ville, prix)

2. **Reducer** (`VentesReducer`) :
   - Reçoit toutes les paires avec la même ville
   - Cumule tous les prix pour chaque ville
   - Produit le total des ventes par ville

Le résultat final sera un fichier contenant chaque ville et son total de ventes.
