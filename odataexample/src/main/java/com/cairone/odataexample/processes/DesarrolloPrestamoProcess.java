package com.cairone.odataexample.processes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.function.Power;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.cairone.odataexample.dtos.PrestamoDesarrolloParamFrmDto;
import com.cairone.odataexample.edm.resources.PrestamoCuotaEdm;

@Component
public class DesarrolloPrestamoProcess {

	private final static Logger LOG = LoggerFactory.getLogger(DesarrolloPrestamoProcess.class);
	private final static int ESCALA = 6;
	public static final String CACHE_NAME = "PRESTAMOS-DESARROLLO";
	
	@Cacheable(CACHE_NAME)
	public Iterable<PrestamoCuotaEdm> desarrollar(PrestamoDesarrolloParamFrmDto prestamoDesarrolloParamFrmDto) {
		
		LOG.info("DESARROLLANDO CUOTAS PARA LOS SIGUIENTES PARAMETROS... {}", prestamoDesarrolloParamFrmDto);
		
		List<PrestamoCuotaEdm> prestamoCuotaDtos = new ArrayList<PrestamoCuotaEdm>();
		
		BigDecimal tasaNominal = BigDecimal.ZERO;
		BigDecimal razon = BigDecimal.ZERO;
		
		if(prestamoDesarrolloParamFrmDto.getTipoTasa().equals("E")) {
			razon = calcularRazonDesdeTasaEfectiva(prestamoDesarrolloParamFrmDto.getTasa(), 30, 360);
			tasaNominal = calcularTasaNominal(razon, 30, 360);
		} else {
			tasaNominal = prestamoDesarrolloParamFrmDto.getTasa();
			razon = calcularRazonDesdeTasaNominal(prestamoDesarrolloParamFrmDto.getTasa(), 30, 360);
		}

		BigDecimal razonGravada = gravar(razon, prestamoDesarrolloParamFrmDto.getAlicuota());
		BigDecimal valorCuota = calculoCuota(prestamoDesarrolloParamFrmDto.getPrestamo(), razonGravada, prestamoDesarrolloParamFrmDto.getCuotas());
		
		Integer diasInteres = 30;
		
		BigDecimal capitalCuota = BigDecimal.ZERO;
		BigDecimal interesGravadoCuota = BigDecimal.ZERO;
		BigDecimal interesDesgravadoCuota = BigDecimal.ZERO;
		BigDecimal ivaCuota = BigDecimal.ZERO;
		BigDecimal montoCuota = BigDecimal.ZERO;
		BigDecimal saldoCapital = prestamoDesarrolloParamFrmDto.getPrestamo();

		// CALCULOS PARA LA CUOTA NUMERO 1

		capitalCuota = calculoCapitalCuota(valorCuota, saldoCapital, razonGravada, 1);
		interesDesgravadoCuota = calcularInteresSimple(saldoCapital, tasaNominal, diasInteres, 360);
		interesGravadoCuota = gravar(interesDesgravadoCuota, prestamoDesarrolloParamFrmDto.getAlicuota());
		ivaCuota = interesGravadoCuota.subtract(interesDesgravadoCuota);
		
		montoCuota = capitalCuota.add(interesGravadoCuota);
		saldoCapital = saldoCapital.subtract(capitalCuota);
		
		PrestamoCuotaEdm prestamoCuotaDto = new PrestamoCuotaEdm(1, capitalCuota, interesDesgravadoCuota, ivaCuota, interesGravadoCuota, montoCuota, saldoCapital);
		prestamoCuotaDto.fixDecimalScale(2);
		prestamoCuotaDtos.add(prestamoCuotaDto);
		

		for(Integer cuotaActual = 2; cuotaActual < prestamoDesarrolloParamFrmDto.getCuotas() + 1; cuotaActual++) {

			capitalCuota = calculoCapitalCuota(valorCuota, saldoCapital, razonGravada, 1);
			interesDesgravadoCuota = calcularInteresSimple(saldoCapital, tasaNominal, diasInteres, 360);
			interesGravadoCuota = gravar(interesDesgravadoCuota, prestamoDesarrolloParamFrmDto.getAlicuota());
			ivaCuota = interesGravadoCuota.subtract(interesDesgravadoCuota);
			
			montoCuota = capitalCuota.add(interesGravadoCuota);
			saldoCapital = saldoCapital.subtract(capitalCuota);

			prestamoCuotaDto = new PrestamoCuotaEdm(cuotaActual, capitalCuota, interesDesgravadoCuota, ivaCuota, interesGravadoCuota, montoCuota, saldoCapital);
			prestamoCuotaDto.fixDecimalScale(2);
			prestamoCuotaDtos.add(prestamoCuotaDto);
			
		}
		
		return prestamoCuotaDtos;
	}

	protected BigDecimal calcularInteresSimple(BigDecimal capital, BigDecimal tasaNominal, Integer dias, Integer modulo) {

		BigDecimal D = new BigDecimal(dias);
		BigDecimal M = new BigDecimal(modulo);
		
		BigDecimal tasa = BigDecimal.ZERO;
		
		if(M.compareTo(BigDecimal.ZERO) != 0) {
			tasa = tasaNominal.multiply(D)
				.multiply(capital)
				.divide(BigDecimal.TEN)
				.divide(BigDecimal.TEN)
				.divide(M, ESCALA, RoundingMode.HALF_UP);
		}
		
		return tasa;
	}
	
	protected BigDecimal calcularTasaNominal(BigDecimal razon, Integer dias, Integer modulo) {
		
		BigDecimal D = new BigDecimal(dias);
		BigDecimal M = new BigDecimal(modulo);
		
		BigDecimal tasa = BigDecimal.ZERO;
		
		if(D.compareTo(BigDecimal.ZERO) != 0) {
			tasa = razon.multiply(M).multiply(BigDecimal.TEN).multiply(BigDecimal.TEN).divide(D, ESCALA, RoundingMode.HALF_UP);
		}
		
		return tasa;
	}
	
	protected BigDecimal calcularTasaEfectiva(BigDecimal razon, Integer dias, Integer modulo) {
		
		Power p = new Power(modulo / dias);
		
		BigDecimal potencia = BigDecimal.valueOf(p.value(razon.add(BigDecimal.ONE).doubleValue()));
		BigDecimal resultado = potencia.subtract(BigDecimal.ONE)
				.multiply(BigDecimal.TEN)
				.multiply(BigDecimal.TEN)
				.setScale(ESCALA, RoundingMode.HALF_UP);
		
		return resultado;
	}
	
	protected BigDecimal calculoCapitalCuota(BigDecimal valorCuota, BigDecimal capital, BigDecimal razon, Integer nroCuota) {
		
		Power p = new Power(nroCuota - 1);
		
		BigDecimal factorCuota = BigDecimal.valueOf(p.value(razon.add(BigDecimal.ONE).doubleValue()));
		BigDecimal factorCapital = razon.multiply(factorCuota);
		
		BigDecimal capitalCuota = valorCuota.multiply(factorCuota).subtract(capital.multiply(factorCapital));
		
		return capitalCuota.setScale(ESCALA, RoundingMode.HALF_UP);
	}

	protected BigDecimal calculoCapitalSaldo(BigDecimal valorCuota, BigDecimal capital, BigDecimal razon, Integer nroCuota) {
		
		Power p = new Power(nroCuota);
		
		BigDecimal factor = BigDecimal.valueOf(p.value(razon.add(BigDecimal.ONE).doubleValue()));
		BigDecimal saldo = factor.multiply(capital).subtract(
				valorCuota.multiply(factor.subtract(BigDecimal.ONE).divide(razon, ESCALA, RoundingMode.HALF_UP)));
		
		return saldo.setScale(ESCALA, RoundingMode.HALF_UP);
	}
	
	protected BigDecimal calculoCuota(BigDecimal capital, BigDecimal razon, Integer totalCuotas) {
		return calculoCuota(capital, BigDecimal.ZERO, razon, totalCuotas);
	}

	protected BigDecimal calculoCuota(BigDecimal capital, BigDecimal opcionCompra, BigDecimal razon, Integer totalCuotas) {

		Power p = new Power(totalCuotas);
		BigDecimal factor = BigDecimal.valueOf(p.value(razon.add(BigDecimal.ONE).doubleValue())).setScale(ESCALA, RoundingMode.HALF_UP);
		
		BigDecimal resultado = capital.multiply(razon).multiply(factor)
				.subtract(opcionCompra.multiply(razon))
				.divide(factor.subtract(BigDecimal.ONE), ESCALA, RoundingMode.HALF_UP);
		
		return resultado;
	}
	
	protected BigDecimal gravar(BigDecimal base, BigDecimal alicuota) {
		
		if(alicuota.compareTo(BigDecimal.ZERO) == 0) {
			return base;
		}
		
		alicuota = alicuota.divide(BigDecimal.TEN).divide(BigDecimal.TEN).add(BigDecimal.ONE);
		
		BigDecimal resultado = base.multiply(alicuota).setScale(ESCALA, RoundingMode.HALF_UP);
		
		return resultado;
	}

	protected BigDecimal calcularRazonDesdeTasaEfectiva(BigDecimal tasa, Integer dias, Integer modulo) {
		
		BigDecimal base = tasa.divide(BigDecimal.TEN).divide(BigDecimal.TEN).add(BigDecimal.ONE);
		BigDecimal exp = BigDecimal.valueOf(dias).divide(BigDecimal.valueOf(modulo), ESCALA, RoundingMode.HALF_UP);
		
		Power p = new Power(exp.doubleValue());
		BigDecimal razon = BigDecimal.valueOf(p.value(base.doubleValue())).subtract(BigDecimal.ONE);
		
		return razon.setScale(ESCALA, RoundingMode.HALF_UP);
	}
	
	protected BigDecimal calcularRazonDesdeTasaNominal(BigDecimal tasa, Integer dias, Integer modulo) {

		BigDecimal D = new BigDecimal(dias);
		BigDecimal M = new BigDecimal(modulo);
		
		BigDecimal resultado = tasa.multiply(D)
				.divide(BigDecimal.TEN)
				.divide(BigDecimal.TEN)
				.divide(M, ESCALA, RoundingMode.HALF_UP);
		
		return resultado;
	}

}
