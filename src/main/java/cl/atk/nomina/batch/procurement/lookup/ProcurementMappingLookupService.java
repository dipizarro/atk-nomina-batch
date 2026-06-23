package cl.atk.nomina.batch.procurement.lookup;

import cl.atk.nomina.batch.procurement.exception.ProcurementMappingException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcurementMappingLookupService {

    private static final String ACTIVE = "V";

    private final GrlMaeItemDetRepository detailRepository;
    private final GrlMaeItemRepository itemRepository;

    public ProcurementMappingLookupService(
            GrlMaeItemDetRepository detailRepository,
            GrlMaeItemRepository itemRepository) {
        this.detailRepository = detailRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public ProcurementItemLookupResult resolveItemForDistribution(
            String codSistem,
            Long codCuenta,
            String codImpsto) {
        String normalizedCodSistem = normalize(codSistem);
        String normalizedCodImpsto = normalize(codImpsto);
        List<GrlMaeItemDetEntity> details =
                detailRepository.findActiveMappingsByAccount(codCuenta, normalizedCodSistem, normalizedCodImpsto, ACTIVE);
        if (details.isEmpty()) {
            List<GrlMaeItemDetEntity> candidates = detailRepository.findActiveMappingsByAccount(codCuenta, ACTIVE);
            throw new ProcurementMappingException(
                    "No ASI item mapping found in GRL_MAE_ITEM_DET for codCuenta=%s codSistem=%s codImpsto=%s availableMappings=%s"
                            .formatted(codCuenta, normalizedCodSistem, normalizedCodImpsto, availableMappings(candidates)));
        }
        if (details.size() > 1) {
            throw new ProcurementMappingException(
                    "Ambiguous ASI item mapping in GRL_MAE_ITEM_DET for codCuenta=%s codSistem=%s codImpsto=%s"
                            .formatted(codCuenta, normalizedCodSistem, normalizedCodImpsto));
        }

        GrlMaeItemDetEntity detail = details.get(0);
        String grlCodItem = normalize(detail.getId().getGrlCodItem());
        String detailCodEmpres = normalize(detail.getId().getCodEmpres());
        Integer detailNumPeriodo = detail.getId().getNumPeriodo();
        boolean itemExists = itemRepository.existsActiveItem(
                detailCodEmpres, detailNumPeriodo, grlCodItem, ACTIVE);
        if (!itemExists) {
            throw new ProcurementMappingException(
                    "ASI item mapping detail exists but master GRL_MAE_ITEM is not active for codEmpres=%s numPeriodo=%s grlCodItem=%s"
                            .formatted(detailCodEmpres, detailNumPeriodo, grlCodItem));
        }

        return new ProcurementItemLookupResult(
                grlCodItem,
                normalize(detail.getCodTipUnid()),
                normalize(detail.getCodTipCntaItems()),
                normalize(detail.getCodContbl()),
                normalize(detail.getId().getCodSistem()),
                detail.getId().getNumPeriodo(),
                normalize(detail.getId().getCodImpsto()),
                normalize(detail.getId().getCodMoneda()),
                detail.getId().getCodCuenta());
    }

    private String availableMappings(List<GrlMaeItemDetEntity> candidates) {
        if (candidates.isEmpty()) {
            return "[]";
        }
        return candidates.stream()
                .map(detail -> "%s/%s/%s/%s".formatted(
                        normalize(detail.getId().getCodSistem()),
                        normalize(detail.getId().getCodImpsto()),
                        normalize(detail.getId().getCodMoneda()),
                        detail.getId().getGrlCodItem()))
                .distinct()
                .limit(10)
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
