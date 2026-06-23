package cl.atk.nomina.batch.procurement.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import cl.atk.nomina.batch.domain.Nomina;
import cl.atk.nomina.batch.domain.NominaHeader;
import cl.atk.nomina.batch.domain.artikos.ArtikosProfileType;
import cl.atk.nomina.batch.procurement.config.ProcurementMappingProperties;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArtikosCompanyMapperTest {

    private final ArtikosCompanyMapper mapper = new ArtikosCompanyMapper(new ProcurementMappingValidator());

    @Test
    void resolvesCompanyFromMsgTo() {
        assertThat(mapper.resolveCodEmpres(ArtikosProfileType.VIDA, nomina("001"), properties())).isEqualTo("001");
        assertThat(mapper.resolveCodEmpres(ArtikosProfileType.GENERALES, nomina("002"), properties())).isEqualTo("002");
        assertThat(mapper.resolveCodEmpres(ArtikosProfileType.VIDA, nomina("ZSGVIDA"), properties())).isEqualTo("001");
        assertThat(mapper.resolveCodEmpres(ArtikosProfileType.GENERALES, nomina("ZSGRALES"), properties())).isEqualTo("002");
    }

    @Test
    void resolvesCompanyFromProfileWhenMsgToIsMissing() {
        assertThat(mapper.resolveCodEmpres(ArtikosProfileType.VIDA, nomina(""), properties())).isEqualTo("001");
        assertThat(mapper.resolveCodEmpres(ArtikosProfileType.GENERALES, nomina(""), properties())).isEqualTo("002");
    }

    private Nomina nomina(String msgTo) {
        return new Nomina(
                "NOMFACTERP",
                "0",
                "ARTIKOS",
                new NominaHeader("ARTIKOS", msgTo, "", "SAF", "NOMFACTERP", "2.0", 15961L, "ZSG_AYP", "", 1),
                List.of());
    }

    private ProcurementMappingProperties properties() {
        return new ProcurementMappingProperties();
    }
}
