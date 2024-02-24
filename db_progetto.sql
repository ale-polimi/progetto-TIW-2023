CREATE TABLE `indirizzo` (
`Id` int NOT NULL AUTO_INCREMENT,
`Citta` varchar(32) NOT NULL,
`Via` varchar(32) NOT NULL,
`Cap` char(5) NOT NULL,
`Numero` int NOT NULL,
PRIMARY KEY (`Id`)
);

CREATE TABLE `utente` (
`IdUtente` int NOT NULL AUTO_INCREMENT,
`Nome` varchar(16) NOT NULL,
`Cognome` varchar(16) NOT NULL,
`Email` varchar(64) NOT NULL,
`Password` varchar(16) NOT NULL,
`IdIndirizzo` int NOT NULL,
PRIMARY KEY (`IdUtente`),
KEY `fk_indirizzo_idx` (`IdIndirizzo`),
CONSTRAINT `fk_indirizzo` FOREIGN KEY (`IdIndirizzo`) REFERENCES `indirizzo` (`Id`)
);

CREATE TABLE `politicaSpedizione` (
`Id` int NOT NULL AUTO_INCREMENT,
`Soglia` int DEFAULT NULL,
PRIMARY KEY (`Id`)
);

CREATE TABLE `fascia` (
`IdFascia` int NOT NULL AUTO_INCREMENT,
`Prezzo` float NOT NULL,
`Min` int NOT NULL,
`Max` int NOT NULL,
PRIMARY KEY (`IdFascia`)
);

CREATE TABLE `fornitore` (
`Id` int NOT NULL AUTO_INCREMENT,
`Nome` varchar(16) NOT NULL,
`Valutazione` varchar(8) NOT NULL,
`IdPoliticaSpedizione` int NOT NULL,
PRIMARY KEY (`Id`),
KEY `fk_politica_fornitore_idx` (`IdPoliticaSpedizione`),
CONSTRAINT `fk_politica_fornitore` FOREIGN KEY (`IdPoliticaSpedizione`) REFERENCES `politicaSpedizione` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `prodotto` (
`Id` int NOT NULL AUTO_INCREMENT,
`Nome` varchar(32) NOT NULL,
`Descrizione` varchar(1024) NOT NULL,
`Categoria` varchar(16) NOT NULL,
`Immagine` longblob,
PRIMARY KEY (`Id`)
);

CREATE TABLE `ordine` (
`Id` int NOT NULL AUTO_INCREMENT,
`Totale` float NOT NULL,
`Data` timestamp DEFAULT NULL,
`IdIndirizzo` int NOT NULL,
`IdUtente` int NOT NULL,
`IdFornitore` int NOT NULL,
PRIMARY KEY (`Id`),
KEY `fk_indirizzo_ordine_idx` (`IdIndirizzo`),
KEY `fk_utente_ordine_idx` (`IdUtente`),
KEY `fk_fornitore_ordine_idx` (`IdFornitore`),
CONSTRAINT `fk_fornitore_ordine` FOREIGN KEY (`IdFornitore`) REFERENCES `fornitore` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `fk_indirizzo_ordine` FOREIGN KEY (`IdIndirizzo`) REFERENCES `indirizzo` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `fk_utente_ordine` FOREIGN KEY (`IdUtente`) REFERENCES `utente` (`IdUtente`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `contenuto` (
`IdOrdine` int NOT NULL,
`IdProdotto` int NOT NULL,
`Quantita` int NOT NULL,
PRIMARY KEY (`IdOrdine`,`IdProdotto`),
KEY `fk_ordine_contenuto_idx` (`IdOrdine`),
KEY `fk_prodotto_contenuto_idx` (`IdProdotto`),
CONSTRAINT `fk_ordine_contenuto` FOREIGN KEY (`IdOrdine`) REFERENCES `ordine` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `fk_prodotto_contenuto` FOREIGN KEY (`IdProdotto`) REFERENCES `prodotto` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE `composizioneFasce` (
`IdPoliticaComp` int NOT NULL,
`IdFasceComp` int NOT NULL,
PRIMARY KEY (`IdPoliticaComp`,`IdFasceComp`),
KEY `fk_politica_composizione_idx` (`IdPoliticaComp`),
KEY `fk_range_composizione_idx` (`IdFasceComp`),
CONSTRAINT `fk_politica_composizione` FOREIGN KEY (`IdPoliticaComp`) REFERENCES `politicaSpedizione` (`Id`) ON DELETE CASCADE ON UPDATE
CASCADE,
CONSTRAINT `fk_range_composizione` FOREIGN KEY (`IdFasceComp`) REFERENCES `fascia` (`IdFascia`) ON DELETE CASCADE ON UPDATE
CASCADE
);

CREATE TABLE `vendita` (
`IdFornitore` int NOT NULL,
`IdProdotto` int NOT NULL,
`Prezzo` float NOT NULL,
PRIMARY KEY (`IdFornitore`,`IdProdotto`),
KEY `fk_fornitore_vendita_idx` (`IdFornitore`),
KEY `fk_prodotto_vendita_idx` (`IdProdotto`),
CONSTRAINT `fk_fornitore_vendita` FOREIGN KEY (`IdFornitore`) REFERENCES `fornitore` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `fk_prodotto_vendita` FOREIGN KEY (`IdProdotto`) REFERENCES `prodotto` (`Id`) ON DELETE CASCADE ON UPDATE CASCADE
);