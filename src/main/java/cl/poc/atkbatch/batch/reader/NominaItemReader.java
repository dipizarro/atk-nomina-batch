package cl.poc.atkbatch.batch.reader;

import cl.poc.atkbatch.domain.Nomina;
import cl.poc.atkbatch.domain.SimulatedNomina;
import cl.poc.atkbatch.service.NominaXmlParserService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.batch.item.ItemReader;

public class NominaItemReader implements ItemReader<SimulatedNomina> {

    private final NominaXmlParserService parserService;
    private final int simulationNominas;
    private Iterator<SimulatedNomina> iterator;

    public NominaItemReader(NominaXmlParserService parserService, int simulationNominas) {
        this.parserService = parserService;
        this.simulationNominas = simulationNominas;
    }

    @Override
    public SimulatedNomina read() {
        if (iterator == null) {
            iterator = buildItems().iterator();
        }
        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<SimulatedNomina> buildItems() {
        Nomina baseNomina = parserService.parseSampleFile();
        Long baseNumeroNomina = baseNomina.cabecera().numeroNomina();
        List<SimulatedNomina> items = new ArrayList<>();

        for (int index = 1; index <= simulationNominas; index++) {
            Long simulatedNumeroNomina = baseNumeroNomina + index - 1L;
            items.add(new SimulatedNomina(
                    baseNumeroNomina,
                    simulatedNumeroNomina,
                    index,
                    baseNomina,
                    baseNomina.documentos(),
                    "%d-%04d".formatted(simulatedNumeroNomina, index)));
        }

        return List.copyOf(items);
    }
}
