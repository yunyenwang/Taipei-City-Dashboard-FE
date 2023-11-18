<!-- Developed by Taipei Urban Intelligence Center 2023 -->

<script setup>
import { ref, computed } from 'vue';
import { useMapStore } from '../../store/mapStore';

const props = defineProps(['chart_config', 'activeChart', 'series', 'map_config']);
const mapStore = useMapStore();

const parseSeries1 = computed(() => {
	chart.hideSeries("新建充電停車格");
/*
	let p = props;
	console.log(props.series);
	console.log(props.series[0]);
	console.log(props.series[1]);
	console.log(props.series[2]);
	if (props.origSeries===undefined) {
		console.log(0);
		let origSeries=[];
		for (let i = 0; i < props.series.length; i++) {
			origSeries.push(props.series[i]);
		}
		p.origSeries=origSeries;
		console.log(p.origSeries);
	}
	if (p.series.length > 2) {
		let p1=p.series[0];
		let p2=p.series[1];
		p.series.splice(0, p.series.length);
		p.series.push(p1);
		p.series.push(p2);
	}
	console.log(1);
	
	return p;
*/	
});

const parseSeries2 = computed(() => {
	let p = props;
//	console.log(props.series[0]);
//	console.log(props.series[1]);
//	console.log(props.series[2]);
/*	
	if (p.series.length > 2) {
		let p3=p.series[2];
		p.series.splice(0, p.series.length);
		p.series.push(p3);
	}
	console.log(p.series);
	console.log(p);
*/	
	return props;
});



var areaOptions = {
  chart: {
    id: "chart2",
    foreColor: "#ccc",
    toolbar: {
      autoSelected: "pan",
      show: false
    }
  },
  colors: ["#00BAEC"],
  stroke: {
    width: 3
  },
  grid: {
    borderColor: "#555",
    clipMarkers: false,
    yaxis: {
      lines: {
        show: false
      }
    }
  },
  dataLabels: {
    enabled: false
  },
  fill: {
    gradient: {
      enabled: true,
      opacityFrom: 0.55,
      opacityTo: 0
    }
  },
  legend: {
  	formatter: (seriesName, opts)=>{
    	if(opts.seriesIndex==2) return ''
    	return seriesName;
  	},
	markers: {
		width: [12,12,0]
	}
  },
  markers: {
    size: 5,
    colors: ["#000524"],
    strokeColor: "#00BAEC",
    strokeWidth: 3
  },
  tooltip: {
    theme: "dark"
  },
  xaxis: {
    type: "text"
  },
  yaxis: {
    min: 0,
    tickAmount: 1
  }
};

var barOptions = {
  chart: {
    id: "chart1",
    foreColor: "#ccc",
    brush: {
      target: "chart2",
      enabled: true
    },
    selection: {
      enabled: true,
      fill: {
        color: "#fff",
        opacity: 0.4
      },
      xaxis: {
		type: "text"
      }
    },

  },
  colors: ["#FF0080"],
  stroke: {
    width: 2
  },
  grid: {
    borderColor: "#444"
  },
  legend: {
  	formatter: (seriesName, opts)=>{
    	if(opts.seriesIndex==2) return seriesName;
    	return '';
  	},
	markers: {
		width: [0,0,12]
	}
  },
  markers: {
    size: 0
  },
  xaxis: {
    type: "text",
    tooltip: {
      enabled: false
    }
  },
  yaxis: {
    tickAmount: 2
  }
};

const selectedIndex = ref(null);
function handleDataSelection(e, chartContext, config) {
	if (!props.chart_config.map_filter) {
		return;
	}
	if (config.dataPointIndex !== selectedIndex.value) {
		mapStore.addLayerFilter(`${props.map_config[0].index}-${props.map_config[0].type}`, props.chart_config.map_filter[0], props.chart_config.map_filter[1][config.dataPointIndex]);
		selectedIndex.value = config.dataPointIndex;
	} else {
		mapStore.clearLayerFilter(`${props.map_config[0].index}-${props.map_config[0].type}`);
		selectedIndex.value = null;
	}
}

function handleAreaSeries(chartContext, config) {
	chartContext.hideSeries("新建充電停車格");
}
function handleBarSeries(chartContext, config) {
	chartContext.hideSeries("汽車充電停車格");
	chartContext.hideSeries("機車充電停車格");
}

</script>

<template>
	<div v-if="activeChart === 'AreaBrushChart'">
		<div>
			<apexchart width="100%" height="130px" type="area" :options="areaOptions" :series="series" @mounted="handleAreaSeries" ></apexchart>
		</div>
		<div>
			<apexchart width="100%" height="130px" type="bar" :options="barOptions" :series="series" @mounted="handleBarSeries" ></apexchart>
		</div>
	</div>
</template>
