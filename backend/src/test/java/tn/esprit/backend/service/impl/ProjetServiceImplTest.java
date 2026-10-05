package tn.esprit.backend.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.backend.entity.Projet;
import tn.esprit.backend.repository.ProjetRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjetServiceImplTest {

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private ProjetServiceImpl projetService;

    @Test
    void addProjet_shouldSaveProjet() {
        Projet projet = new Projet();

        when(projetRepository.save(projet)).thenReturn(projet);

        Projet result = projetService.addProjet(projet);

        assertNotNull(result);
        assertEquals(projet, result);
        verify(projetRepository).save(projet);
    }

    @Test
    void getProjetById_shouldReturnProjetWhenExists() {
        Long id = 1L;
        Projet projet = new Projet();

        when(projetRepository.findById(id)).thenReturn(Optional.of(projet));

        Projet result = projetService.getProjetById(id);

        assertNotNull(result);
        assertEquals(projet, result);
        verify(projetRepository).findById(id);
    }
}
