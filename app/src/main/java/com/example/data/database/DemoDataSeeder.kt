package com.example.data.database

import com.example.data.model.Item
import com.example.data.model.ItemBoxCrossRef
import com.example.data.model.ItemTagCrossRef
import com.example.data.model.Location
import com.example.data.model.StorageBox
import com.example.data.model.Tag

object DemoDataSeeder {

    suspend fun seedIfEmpty(database: AppDatabase) {
        val count = database.inventoryDao().getTotalItemsCount()
        if (count == 0) {
            seed(database)
        }
    }

    suspend fun seed(database: AppDatabase) {
        val locationDao = database.locationDao()
        val boxDao = database.boxDao()
        val tagDao = database.tagDao()
        val itemDao = database.itemDao()

        // 1. Hierarchical Locations
        // Maison (Root)
        val locMaisonId = locationDao.insertLocation(
            Location(name = "Maison", parentId = null, description = "Résidence principale")
        )
        // Garage (Child of Maison)
        val locGarageId = locationDao.insertLocation(
            Location(name = "Garage", parentId = locMaisonId, description = "Garage et atelier")
        )
        // Étagère 3 (Child of Garage)
        val locEtagere3Id = locationDao.insertLocation(
            Location(name = "Étagère 3", parentId = locGarageId, description = "Rangement métallique nord")
        )
        // Bureau (Child of Maison)
        val locBureauId = locationDao.insertLocation(
            Location(name = "Bureau", parentId = locMaisonId, description = "Pièce de travail")
        )
        // Placard Bureau (Child of Bureau)
        val locPlacardBureauId = locationDao.insertLocation(
            Location(name = "Placard", parentId = locBureauId, description = "Placard mural bureau")
        )
        // Cave (Child of Maison)
        val locCaveId = locationDao.insertLocation(
            Location(name = "Cave", parentId = locMaisonId, description = "Cave sous-sol")
        )

        // 2. Boxes
        val boxBricolageId = boxDao.insertBox(
            StorageBox(
                code = "BOX-0001",
                name = "Boîte Bricolage",
                label = "Bricolage",
                description = "Outils manuels et quincaillerie courante",
                locationId = locGarageId
            )
        )
        val boxCablesId = boxDao.insertBox(
            StorageBox(
                code = "BOX-0002",
                name = "Boîte Câbles",
                label = "Câbles",
                description = "Câbles USB, audio et cordons divers",
                locationId = locEtagere3Id
            )
        )
        val boxInformatiqueId = boxDao.insertBox(
            StorageBox(
                code = "BOX-0003",
                name = "Boîte Informatique",
                label = "Informatique",
                description = "Périphériques PC, adaptateurs et stockage",
                locationId = locPlacardBureauId
            )
        )
        val boxBureauId = boxDao.insertBox(
            StorageBox(
                code = "BOX-0004",
                name = "Boîte Bureau",
                label = "Bureau",
                description = "Fournitures et consommables bureau",
                locationId = locBureauId
            )
        )
        val boxElectriciteId = boxDao.insertBox(
            StorageBox(
                code = "BOX-0005",
                name = "Boîte Électricité",
                label = "Électricité",
                description = "Piles, ampoules et connecteurs électriques",
                locationId = locGarageId
            )
        )

        // 3. Tags
        val tagElectroniqueId = tagDao.insertTag(Tag(name = "électronique", colorHex = "#3B82F6"))
        val tagBricolageId = tagDao.insertTag(Tag(name = "bricolage", colorHex = "#F97316"))
        val tagInformatiqueId = tagDao.insertTag(Tag(name = "informatique", colorHex = "#8B5CF6"))
        val tagCableId = tagDao.insertTag(Tag(name = "câble", colorHex = "#06B6D4"))
        val tagUsbCId = tagDao.insertTag(Tag(name = "USB-C", colorHex = "#6366F1"))
        val tagImportantId = tagDao.insertTag(Tag(name = "important", colorHex = "#EF4444"))
        val tagAReparerId = tagDao.insertTag(Tag(name = "à réparer", colorHex = "#EAB308"))
        val tagVintageId = tagDao.insertTag(Tag(name = "vintage", colorHex = "#EC4899"))
        val tagVoyageId = tagDao.insertTag(Tag(name = "voyage", colorHex = "#10B981"))

        // 4. Items & Quantities
        // Câble USB-C : 7 exemplaires (3 dans Câbles, 2 dans Informatique, 2 dans Bureau)
        val itemCableUsbCId = itemDao.insertItem(
            Item(
                name = "Câble USB-C",
                description = "Câble USB-C vers USB-C tressé 1m 100W",
                category = "Câblage"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemCableUsbCId, tagCableId))
        itemDao.insertItemTagRef(ItemTagCrossRef(itemCableUsbCId, tagUsbCId))
        itemDao.insertItemTagRef(ItemTagCrossRef(itemCableUsbCId, tagInformatiqueId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemCableUsbCId, boxCablesId, 3))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemCableUsbCId, boxInformatiqueId, 2))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemCableUsbCId, boxBureauId, 2))

        // Piles AA : 20 exemplaires (12 dans Électricité, 5 dans Bureau, 3 dans Informatique)
        val itemPilesAaId = itemDao.insertItem(
            Item(
                name = "Piles AA",
                description = "Piles alcalines 1.5V haute endurance",
                category = "Alimentation"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemPilesAaId, tagElectroniqueId))
        itemDao.insertItemTagRef(ItemTagCrossRef(itemPilesAaId, tagImportantId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemPilesAaId, boxElectriciteId, 12))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemPilesAaId, boxBureauId, 5))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemPilesAaId, boxInformatiqueId, 3))

        // Tournevis cruciforme : 2 exemplaires dans Boîte Bricolage
        val itemTournevisId = itemDao.insertItem(
            Item(
                name = "Tournevis cruciforme",
                description = "Tournevis PH2 manche ergonomique bi-matière",
                category = "Outillage"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemTournevisId, tagBricolageId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemTournevisId, boxBricolageId, 2))

        // Adaptateur HDMI : 3 exemplaires dans Boîte Informatique
        val itemHdmiId = itemDao.insertItem(
            Item(
                name = "Adaptateur HDMI",
                description = "Adaptateur HDMI vers USB-C support 4K 60Hz",
                category = "Vidéo"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemHdmiId, tagInformatiqueId))
        itemDao.insertItemTagRef(ItemTagCrossRef(itemHdmiId, tagCableId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemHdmiId, boxInformatiqueId, 3))

        // Clé USB : 4 exemplaires (2 dans Informatique, 2 dans Bureau)
        val itemCleUsbId = itemDao.insertItem(
            Item(
                name = "Clé USB 64Go",
                description = "Clé USB 3.0 compacte en aluminium",
                category = "Stockage"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemCleUsbId, tagInformatiqueId))
        itemDao.insertItemTagRef(ItemTagCrossRef(itemCleUsbId, tagImportantId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemCleUsbId, boxInformatiqueId, 2))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemCleUsbId, boxBureauId, 2))

        // Chargeur : 2 exemplaires dans Bureau
        val itemChargeurId = itemDao.insertItem(
            Item(
                name = "Chargeur rapide 65W",
                description = "Chargeur mural GaN triple port USB-C / USB-A",
                category = "Alimentation"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemChargeurId, tagElectroniqueId))
        itemDao.insertItemTagRef(ItemTagCrossRef(itemChargeurId, tagVoyageId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemChargeurId, boxBureauId, 2))

        // Ruban adhésif : 3 exemplaires dans Bricolage
        val itemAdhesifId = itemDao.insertItem(
            Item(
                name = "Ruban adhésif toilé",
                description = "Rouleau résistant pour emballage et fixation",
                category = "Consommable"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemAdhesifId, tagBricolageId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemAdhesifId, boxBricolageId, 3))

        // Ampoule LED : 6 exemplaires dans Électricité
        val itemAmpouleId = itemDao.insertItem(
            Item(
                name = "Ampoule LED E27",
                description = "Ampoules blanc chaud 2700K 800 lumens",
                category = "Éclairage"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemAmpouleId, tagBricolageId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemAmpouleId, boxElectriciteId, 6))

        // Souris : 1 exemplaire dans Bureau
        val itemSourisId = itemDao.insertItem(
            Item(
                name = "Souris sans fil",
                description = "Souris optique Bluetooth et 2.4GHz rechargeable",
                category = "Informatique"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemSourisId, tagInformatiqueId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemSourisId, boxBureauId, 1))

        // Casque audio : 1 exemplaire dans Bureau
        val itemCasqueId = itemDao.insertItem(
            Item(
                name = "Casque audio",
                description = "Casque supra-aural pliable avec micro",
                category = "Audio"
            )
        )
        itemDao.insertItemTagRef(ItemTagCrossRef(itemCasqueId, tagElectroniqueId))
        itemDao.insertItemTagRef(ItemTagCrossRef(itemCasqueId, tagVintageId))
        itemDao.insertOrUpdateBoxQuantity(ItemBoxCrossRef(itemCasqueId, boxBureauId, 1))
    }

    suspend fun clearAll(database: AppDatabase) {
        database.clearAllTables()
    }
}
