# LOGS.md — TP 11 : logs SQL commentés et vérifications en base

**Étudiant :** Mohamed Elhafed Kah — matricule ESP 251015

Base : MariaDB, schéma `hb-01-one-to-one-uni` recréé avec `sql/script.sql` avant la séquence.
Exercices lancés **dans l'ordre** ci-dessous, un seul appel décommenté à la fois dans le `CommandLineRunner`.
Logs obtenus avec `logging.level.org.hibernate.SQL=trace` (requêtes) et `logging.level.org.hibernate.orm.jdbc.bind=trace` (valeurs liées aux `?`).

---

## Exercice 5.1 — `@OneToOne` unidirectionnel : création en cascade

Appel : `createInstructor(appDAO)`

```
Saving instructor: Instructor{id=0, firstName='Madhu', lastName='Patel', email='madhu@test.com', instructorDetail=InstructorDetail{id=0, youtubeChannel='http://www.youtube.com/@madhu', hobby='Guitar'}}
insert into instructor_detail (hobby,youtube_channel) values (?,?)
binding parameter (1:VARCHAR) <- [Guitar]
binding parameter (2:VARCHAR) <- [http://www.youtube.com/@madhu]
insert into instructor (email,first_name,instructor_detail_id,last_name) values (?,?,?,?)
binding parameter (1:VARCHAR) <- [madhu@test.com]
binding parameter (2:VARCHAR) <- [Madhu]
binding parameter (3:INTEGER) <- [1]
binding parameter (4:VARCHAR) <- [Patel]
Done!
```

**Analyse :**
- On n'appelle que `persist(instructor)`, mais **deux `insert`** sont exécutés : c'est la cascade `CascadeType.ALL`.
- **Ordre :** `instructor_detail` d'abord, puis `instructor`. Hibernate a besoin de l'id du détail (1) pour remplir la FK `instructor_detail_id`.
- `id=0` dans le `toString` avant le `save` : l'id n'est généré par la base (`AUTO_INCREMENT`) qu'au moment de l'insert.

**Vérification :**

```
SELECT * FROM instructor
+----+------------+-----------+----------------+----------------------+
| id | first_name | last_name | email          | instructor_detail_id |
+----+------------+-----------+----------------+----------------------+
|  1 | Madhu      | Patel     | madhu@test.com |                    1 |
+----+------------+-----------+----------------+----------------------+
SELECT * FROM instructor_detail
+----+-------------------------------+--------+
| id | youtube_channel               | hobby  |
+----+-------------------------------+--------+
|  1 | http://www.youtube.com/@madhu | Guitar |
+----+-------------------------------+--------+
```

`instructor.instructor_detail_id = 1` pointe bien vers la ligne de détail.

---

## Exercice 5.2 — `@OneToOne` : récupération et suppression en cascade

### Find — `findInstructor(appDAO)` (id = 1)

```
select i1_0.id,i1_0.email,i1_0.first_name,id1_0.id,id1_0.hobby,id1_0.youtube_channel,i1_0.last_name from instructor i1_0 left join instructor_detail id1_0 on id1_0.id=i1_0.instructor_detail_id where i1_0.id=?
binding parameter (1:INTEGER) <- [1]
Instructor: Instructor{id=1, firstName='Madhu', lastName='Patel', email='madhu@test.com', instructorDetail=InstructorDetail{id=1, youtubeChannel='http://www.youtube.com/@madhu', hobby='Guitar'}}
Detail: InstructorDetail{id=1, youtubeChannel='http://www.youtube.com/@madhu', hobby='Guitar'}
```

**Analyse :** **une seule requête** avec un `left join` sur `instructor_detail`. Le `@OneToOne` est **EAGER par défaut** : le détail est chargé en même temps que l'instructeur, sans requête supplémentaire.

### Delete — `deleteInstructor(appDAO)` (id = 1)

**Avant :**

```
SELECT * FROM instructor
+----+------------+-----------+----------------+----------------------+
| id | first_name | last_name | email          | instructor_detail_id |
+----+------------+-----------+----------------+----------------------+
|  1 | Madhu      | Patel     | madhu@test.com |                    1 |
+----+------------+-----------+----------------+----------------------+
SELECT * FROM instructor_detail
+----+-------------------------------+--------+
| id | youtube_channel               | hobby  |
+----+-------------------------------+--------+
|  1 | http://www.youtube.com/@madhu | Guitar |
+----+-------------------------------+--------+
```

**Log :**

```
Deleting instructor id: 1
select i1_0.id,i1_0.email,i1_0.first_name,id1_0.id,id1_0.hobby,id1_0.youtube_channel,i1_0.last_name from instructor i1_0 left join instructor_detail id1_0 on id1_0.id=i1_0.instructor_detail_id where i1_0.id=?
binding parameter (1:INTEGER) <- [1]
select c1_0.instructor_id,c1_0.id,c1_0.title from course c1_0 where c1_0.instructor_id=?
binding parameter (1:INTEGER) <- [1]
delete from instructor where id=?
binding parameter (1:INTEGER) <- [1]
delete from instructor_detail where id=?
binding parameter (1:INTEGER) <- [1]
Done!
```

**Analyse :**
- 1er `select` : chargement de l'instructeur (avec son détail) par `find`.
- 2e `select` : chargement de ses cours. Le DAO les détache avant la suppression, sinon la FK `course.instructor_id` bloquerait le `DELETE`. Ici il n'y en a aucun.
- **Ordre des `delete` :** `instructor` **puis** `instructor_detail`. C'est l'ordre inverse de la création : on supprime d'abord la ligne qui porte la FK.
- Le `delete from instructor_detail` vient de la cascade `ALL` (donc `REMOVE`).

**Après :**

```
SELECT * FROM instructor
Empty set
SELECT * FROM instructor_detail
Empty set
```

---

## Exercice 5.3 — `@OneToOne` bidirectionnel : `mappedBy` et suppression du détail seul

Avant de commencer, une nouvelle paire instructeur/détail est créée avec `createInstructor` (même log qu'en 5.1). Les nouveaux ids sont **2**, car `AUTO_INCREMENT` ne réutilise pas les ids supprimés.

### Find depuis le détail — `findInstructorDetail(appDAO)` (id = 2)

```
select id1_0.id,id1_0.hobby,i1_0.id,i1_0.email,i1_0.first_name,i1_0.last_name,id1_0.youtube_channel from instructor_detail id1_0 left join instructor i1_0 on id1_0.id=i1_0.instructor_detail_id where id1_0.id=?
binding parameter (1:INTEGER) <- [2]
Detail: InstructorDetail{id=2, youtubeChannel='http://www.youtube.com/@madhu', hobby='Guitar'}
Associated instructor: Instructor{id=2, firstName='Madhu', lastName='Patel', email='madhu@test.com', instructorDetail=InstructorDetail{id=2, youtubeChannel='http://www.youtube.com/@madhu', hobby='Guitar'}}
```

**Analyse :**
- On part maintenant du détail, et on retrouve son instructeur grâce à `mappedBy = "instructorDetail"`.
- La jointure se fait sur `i1_0.instructor_detail_id` : c'est **la même FK, toujours dans la table `instructor`**. Le côté inverse n'ajoute aucune colonne.

### Suppression du détail seul — `deleteInstructorDetail(appDAO)` (id = 2)

Configuration : **`REMOVE` retiré** de la cascade de `InstructorDetail.instructor`, et **lien cassé** dans le DAO avec `detail.getInstructor().setInstructorDetail(null)`.

**Avant :**

```
SELECT * FROM instructor
+----+------------+-----------+----------------+----------------------+
| id | first_name | last_name | email          | instructor_detail_id |
+----+------------+-----------+----------------+----------------------+
|  2 | Madhu      | Patel     | madhu@test.com |                    2 |
+----+------------+-----------+----------------+----------------------+
SELECT * FROM instructor_detail
+----+-------------------------------+--------+
| id | youtube_channel               | hobby  |
+----+-------------------------------+--------+
|  2 | http://www.youtube.com/@madhu | Guitar |
+----+-------------------------------+--------+
```

**Log :**

```
Deleting instructor detail id: 2
select id1_0.id,id1_0.hobby,i1_0.id,i1_0.email,i1_0.first_name,i1_0.last_name,id1_0.youtube_channel from instructor_detail id1_0 left join instructor i1_0 on id1_0.id=i1_0.instructor_detail_id where id1_0.id=?
binding parameter (1:INTEGER) <- [2]
update instructor set email=?,first_name=?,instructor_detail_id=?,last_name=? where id=?
binding parameter (1:VARCHAR) <- [madhu@test.com]
binding parameter (2:VARCHAR) <- [Madhu]
binding parameter (3:INTEGER) <- [null]
binding parameter (4:VARCHAR) <- [Patel]
binding parameter (5:INTEGER) <- [2]
delete from instructor_detail where id=?
binding parameter (1:INTEGER) <- [2]
Done!
```

**Analyse :**
- `update instructor ... instructor_detail_id = null` vient de `setInstructorDetail(null)`. La FK est vidée **avant** la suppression, donc la contrainte `FK_DETAIL` est respectée.
- `delete from instructor_detail` : seul le détail est supprimé.
- **Aucun `delete from instructor`**, puisqu'il n'y a plus de `REMOVE` dans la cascade.

**Après :**

```
SELECT * FROM instructor
+----+------------+-----------+----------------+----------------------+
| id | first_name | last_name | email          | instructor_detail_id |
+----+------------+-----------+----------------+----------------------+
|  2 | Madhu      | Patel     | madhu@test.com |                 NULL |
+----+------------+-----------+----------------+----------------------+
SELECT * FROM instructor_detail
Empty set
```

L'instructeur **existe toujours**, `instructor_detail_id` est passé à **`NULL`** et le détail a disparu.

### À comparer : même suppression avec `CascadeType.ALL`, sans casser le lien

Expérience refaite sur une base neuve, en remettant `cascade = CascadeType.ALL` sur `InstructorDetail.instructor` et en retirant `setInstructorDetail(null)` du DAO.

**Avant :**

```
SELECT * FROM instructor
+----+------------+-----------+-----------------------+----------------------+
| id | first_name | last_name | email                 | instructor_detail_id |
+----+------------+-----------+-----------------------+----------------------+
|  1 | Susan      | Public    | susan.public@test.com |                    1 |
+----+------------+-----------+-----------------------+----------------------+
SELECT * FROM instructor_detail
+----+-------------------------------+-------------+
| id | youtube_channel               | hobby       |
+----+-------------------------------+-------------+
|  1 | http://www.youtube.com/@susan | Video Games |
+----+-------------------------------+-------------+
```

**Log :**

```
Deleting instructor detail id: 1
select id1_0.id,id1_0.hobby,i1_0.id,i1_0.email,i1_0.first_name,i1_0.last_name,id1_0.youtube_channel from instructor_detail id1_0 left join instructor i1_0 on id1_0.id=i1_0.instructor_detail_id where id1_0.id=?
binding parameter (1:INTEGER) <- [1]
delete from instructor where id=?
binding parameter (1:INTEGER) <- [1]
delete from instructor_detail where id=?
binding parameter (1:INTEGER) <- [1]
Done!
```

**Après :**

```
SELECT * FROM instructor
Empty set
SELECT * FROM instructor_detail
Empty set
```

**Différence :** il n'y a **plus d'`update`**, mais un **`delete from instructor`** en plus. Le `REMOVE` se propage du détail vers l'instructeur, et **l'instructeur est supprimé lui aussi**. C'est exactement ce qu'on voulait éviter.

---

## Exercice 5.4 — `@OneToMany` / `@ManyToOne` bidirectionnel (Instructor ⇄ Course)

Appel : `createInstructorWithCourses(appDAO)` (suite de la séquence principale, après 5.3)

```
Saving instructor: Instructor{id=0, firstName='Susan', lastName='Public', email='susan.public@test.com', instructorDetail=InstructorDetail{id=0, youtubeChannel='http://www.youtube.com/@susan', hobby='Video Games'}}
insert into instructor_detail (hobby,youtube_channel) values (?,?)
binding parameter (1:VARCHAR) <- [Video Games]
binding parameter (2:VARCHAR) <- [http://www.youtube.com/@susan]
insert into instructor (email,first_name,instructor_detail_id,last_name) values (?,?,?,?)
binding parameter (1:VARCHAR) <- [susan.public@test.com]
binding parameter (2:VARCHAR) <- [Susan]
binding parameter (3:INTEGER) <- [3]
binding parameter (4:VARCHAR) <- [Public]
insert into course (instructor_id,title) values (?,?)
binding parameter (1:INTEGER) <- [3]
binding parameter (2:VARCHAR) <- [Air Guitar - The Ultimate Guide]
insert into course (instructor_id,title) values (?,?)
binding parameter (1:INTEGER) <- [3]
binding parameter (2:VARCHAR) <- [The Pinball Masterclass]
Done!
```

**Analyse :**
- Un seul `save(instructor)` produit **4 `insert`** : le détail (cascade `ALL`), l'instructeur, puis les **2 cours** (cascade `PERSIST` sur `Instructor.courses`).
- **Ordre :** détail → instructeur → cours. Chaque ligne a besoin de l'id de la précédente pour sa FK.
- Les cours sont insérés **directement avec `instructor_id = 3`**, sans `update` ensuite. C'est parce que `Course` est le côté propriétaire et que `add()` a renseigné `course.setInstructor(this)`.

**Vérification :**

```
SELECT id, title, instructor_id FROM course
+----+---------------------------------+---------------+
| id | title                           | instructor_id |
+----+---------------------------------+---------------+
|  1 | Air Guitar - The Ultimate Guide |             3 |
|  2 | The Pinball Masterclass         |             3 |
+----+---------------------------------+---------------+
```

Les deux cours portent le même `instructor_id`.

### Expérience : le piège de la synchronisation (sans `tempCourse.setInstructor(this)`)

Expérience refaite sur une base neuve, avec la ligne `tempCourse.setInstructor(this)` retirée de `add()`.

```
insert into instructor_detail (hobby,youtube_channel) values (?,?)
binding parameter (1:VARCHAR) <- [Video Games]
binding parameter (2:VARCHAR) <- [http://www.youtube.com/@susan]
insert into instructor (email,first_name,instructor_detail_id,last_name) values (?,?,?,?)
binding parameter (1:VARCHAR) <- [susan.public@test.com]
binding parameter (2:VARCHAR) <- [Susan]
binding parameter (3:INTEGER) <- [1]
binding parameter (4:VARCHAR) <- [Public]
insert into course (instructor_id,title) values (?,?)
binding parameter (1:INTEGER) <- [null]
binding parameter (2:VARCHAR) <- [Air Guitar - The Ultimate Guide]
insert into course (instructor_id,title) values (?,?)
binding parameter (1:INTEGER) <- [null]
binding parameter (2:VARCHAR) <- [The Pinball Masterclass]
Done!
```

```
SELECT id, title, instructor_id FROM course
+----+---------------------------------+---------------+
| id | title                           | instructor_id |
+----+---------------------------------+---------------+
|  1 | Air Guitar - The Ultimate Guide |          NULL |
|  2 | The Pinball Masterclass         |          NULL |
+----+---------------------------------+---------------+
```

**Analyse :**
- Les cours sont bien insérés, car la cascade `PERSIST` part de la liste `courses`.
- En revanche, `binding parameter (1:INTEGER) <- [null]` : **`instructor_id` reste `NULL`**.
- Hibernate ne lit que le côté propriétaire (`Course.instructor`), qui n'a pas été renseigné. Remplir uniquement la liste du côté inverse ne suffit pas.

---

## Exercice 5.5 — `@OneToMany` unidirectionnel (Course → Review)

### Création — `createCourseAndReviews(appDAO)`

```
Saving the course: Course{id=0, title='Pacman - How To Score One Million Points'}
insert into course (instructor_id,title) values (?,?)
binding parameter (1:INTEGER) <- [null]
binding parameter (2:VARCHAR) <- [Pacman - How To Score One Million Points]
insert into review (comment) values (?)
binding parameter (1:VARCHAR) <- [Great course... loved it!]
insert into review (comment) values (?)
binding parameter (1:VARCHAR) <- [Cool course, job well done.]
insert into review (comment) values (?)
binding parameter (1:VARCHAR) <- [What a dumb course, you are an idiot!]
update review set course_id=? where id=?
binding parameter (1:INTEGER) <- [3]
binding parameter (2:INTEGER) <- [1]
update review set course_id=? where id=?
binding parameter (1:INTEGER) <- [3]
binding parameter (2:INTEGER) <- [2]
update review set course_id=? where id=?
binding parameter (1:INTEGER) <- [3]
binding parameter (2:INTEGER) <- [3]
Done! id du cours créé = 3 (à utiliser dans deleteCourse)
```

**Analyse :**
- `insert` du cours (`instructor_id = null` : ce cours n'a pas d'instructeur), puis **3 `insert` dans `review` sans `course_id`**, grâce à la cascade `ALL`.
- Ensuite viennent **3 `update review set course_id = 3`**. La classe `Review` ne connaît pas son cours (aucun champ `Course`) : Hibernate insère d'abord les avis, puis renseigne la FK depuis `Course`, qui est propriétaire via `@OneToMany` + `@JoinColumn`.
- Comparé au 5.4 (insert direct avec la FK), le `@OneToMany` unidirectionnel coûte donc **une requête de plus par avis**.

**Vérification :**

```
SELECT id, comment, course_id FROM review
+----+---------------------------------------+-----------+
| id | comment                               | course_id |
+----+---------------------------------------+-----------+
|  1 | Great course... loved it!             |         3 |
|  2 | Cool course, job well done.           |         3 |
|  3 | What a dumb course, you are an idiot! |         3 |
+----+---------------------------------------+-----------+
```

Les trois avis portent le `course_id` du cours créé (3).

### Suppression en cascade — `deleteCourse(appDAO)` (id = 3)

**Avant :**

```
SELECT id, title, instructor_id FROM course
+----+------------------------------------------+---------------+
| id | title                                    | instructor_id |
+----+------------------------------------------+---------------+
|  1 | Air Guitar - The Ultimate Guide          |             3 |
|  2 | The Pinball Masterclass                  |             3 |
|  3 | Pacman - How To Score One Million Points |          NULL |
+----+------------------------------------------+---------------+
SELECT id, comment, course_id FROM review
+----+---------------------------------------+-----------+
| id | comment                               | course_id |
+----+---------------------------------------+-----------+
|  1 | Great course... loved it!             |         3 |
|  2 | Cool course, job well done.           |         3 |
|  3 | What a dumb course, you are an idiot! |         3 |
+----+---------------------------------------+-----------+
```

**Log :**

```
Deleting course id: 3
select c1_0.id,i1_0.id,i1_0.email,i1_0.first_name,id1_0.id,id1_0.hobby,id1_0.youtube_channel,i1_0.last_name,c1_0.title from course c1_0 left join instructor i1_0 on i1_0.id=c1_0.instructor_id left join instructor_detail id1_0 on id1_0.id=i1_0.instructor_detail_id where c1_0.id=?
binding parameter (1:INTEGER) <- [3]
select r1_0.course_id,r1_0.id,r1_0.comment from review r1_0 where r1_0.course_id=?
binding parameter (1:INTEGER) <- [3]
update review set course_id=null where course_id=?
binding parameter (1:INTEGER) <- [3]
delete from review where id=?
binding parameter (1:INTEGER) <- [1]
delete from review where id=?
binding parameter (1:INTEGER) <- [2]
delete from review where id=?
binding parameter (1:INTEGER) <- [3]
delete from course where id=?
binding parameter (1:INTEGER) <- [3]
Done!
```

**Analyse :**
- 1er `select` : chargement du cours. Son `@ManyToOne` vers l'instructeur est EAGER, d'où les `left join`.
- 2e `select` : chargement des reviews. La collection est `LAZY`, elle n'est lue qu'au moment où la cascade `REMOVE` en a besoin.
- `update review set course_id = null` : Hibernate détache d'abord les avis du cours, ce qui est typique du `@OneToMany` unidirectionnel.
- **3 `delete from review`** (cascade `ALL`), **puis** `delete from course` : les enfants sont supprimés avant le parent.
- **Aucun `delete from instructor`**, puisque `Course.instructor` n'a pas de `REMOVE`.

**Après :**

```
SELECT id, title, instructor_id FROM course
+----+---------------------------------+---------------+
| id | title                           | instructor_id |
+----+---------------------------------+---------------+
|  1 | Air Guitar - The Ultimate Guide |             3 |
|  2 | The Pinball Masterclass         |             3 |
+----+---------------------------------+---------------+
SELECT id, comment, course_id FROM review
Empty set
SELECT id, first_name FROM instructor
+----+------------+
| id | first_name |
+----+------------+
|  2 | Madhu      |
|  3 | Susan      |
+----+------------+
```

Le cours et ses trois avis ont disparu. Les autres cours et les instructeurs sont intacts.

---

## Récapitulatif des cascades observées

| Exercice | Opération Java | Requêtes SQL (dans l'ordre) |
|---|---|---|
| 5.1 | `persist(instructor)` | insert `instructor_detail` → insert `instructor` |
| 5.2 | `remove(instructor)` | delete `instructor` → delete `instructor_detail` |
| 5.3 | `remove(detail)` sans `REMOVE` + lien cassé | update `instructor` (FK → NULL) → delete `instructor_detail` |
| 5.3 (comparaison) | `remove(detail)` avec `ALL` | delete `instructor` → delete `instructor_detail` |
| 5.4 | `persist(instructor)` + `add()` | insert `instructor_detail` → insert `instructor` → 2 × insert `course` (FK renseignée) |
| 5.4 (piège) | `add()` sans `setInstructor` | 2 × insert `course` avec `instructor_id = NULL` |
| 5.5 | `persist(course)` | insert `course` → 3 × insert `review` → 3 × update `review` (FK) |
| 5.5 | `remove(course)` | update `review` (FK → NULL) → 3 × delete `review` → delete `course` |
