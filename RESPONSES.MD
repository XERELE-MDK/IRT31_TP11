# REPONSES.md — TP 11 : Relations JPA (`@OneToOne`, `@OneToMany` / `@ManyToOne`)

**Étudiant :** Mohamed Elhafed Kah — matricule ESP 251015
**Module :** Spring Boot 4 — JPA / Hibernate : mappings avancés (JDK 25)

---

## 1. Le côté propriétaire

C'est le côté qui **porte la clé étrangère** en base. Dans le code, c'est celui qui a **`@JoinColumn`** et **pas de `mappedBy`**.

Pour `Instructor ⇄ Course`, la FK est dans la table **`course`** (colonne `instructor_id`) : `Course` est donc le côté propriétaire.

---

## 2. `mappedBy`

`mappedBy` indique le **côté inverse** : « la relation est déjà gérée par ce champ de l'autre classe ».
Exemple : `@OneToMany(mappedBy = "instructor")` renvoie au champ `Course.instructor`.

Il ne crée **aucune colonne** car la relation n'a besoin que **d'une seule FK**, déjà définie par le côté propriétaire. Le côté inverse sert seulement à naviguer en Java (`instructor.getCourses()`).

---

## 3. Supprimer le détail seul (Ex. 5.3)

Il faut faire **les deux** :

- **Retirer `REMOVE`** : sinon la suppression du détail se propage et **supprime aussi l'instructeur**.
- **Casser le lien** (`setInstructorDetail(null)`) : sinon l'instructeur pointe encore vers le détail et le `DELETE` **viole la clé étrangère**.

Si on ne fait que l'un des deux : soit l'instructeur est supprimé, soit on obtient une erreur de FK.

---

## 4. La méthode `add(Course)`

Elle met à jour **les deux côtés** en un seul appel : elle ajoute le cours à la liste **et** fait `course.setInstructor(this)`.

Si on oublie `setInstructor(this)`, le cours est bien inséré mais **`instructor_id` reste `NULL`**, car Hibernate ne lit que le côté propriétaire (`Course.instructor`).

---

## 5. Pourquoi `Course` est propriétaire

Dans une relation 1–N, la FK se place **toujours du côté N** : chaque cours stocke l'id de son instructeur. La table `instructor` ne peut pas stocker une liste de cours dans une seule colonne.

Le côté propriétaire étant celui qui porte la FK, c'est donc `Course`.

---

## 6. `Course → Review` unidirectionnelle

Une review n'a pas besoin de connaître son cours : on y accède toujours depuis le cours.

Ce qui l'en empêche dans le code : la classe `Review` **n'a aucun champ `Course` ni `@ManyToOne`**. La FK `course_id` est gérée uniquement par `Course` (`@OneToMany` + `@JoinColumn`).

---

## 7. `ALL` sur Review, mais pas de `REMOVE` sur Course

- **Course → Review : `ALL`**. Une review n'existe pas sans son cours : si on supprime le cours, ses reviews doivent disparaître aussi.
- **Instructor ⇄ Course : sans `REMOVE`**. Instructeur et cours vivent **indépendamment** : supprimer un cours ne doit pas supprimer l'instructeur, et inversement.

---

## 8. Les propriétés de log

- `logging.level.org.hibernate.SQL=trace` : affiche **les requêtes SQL** générées, avec des `?` à la place des valeurs.
- `...jdbc.bind=trace` : affiche en plus **les valeurs réelles** de chaque `?` (ex. `binding parameter (1:VARCHAR) <- [Madhu]`).

La seconde permet de vérifier, par exemple, que `instructor_id` reçoit bien le bon id (ou `null`).
